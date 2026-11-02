//////////////////////////////////////////////////////////////////////////////
// SurveyServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag("IntegrationStage")
public abstract
class SurveyServiceTest
{
    @Test
    public void
    testSubmitSurveyResponse()
    {
        SurveyResponseData submitted = submit(Map.of("satisfied",4,"needs",3,"recommend",5),"  Works well  ");

        Assertions.assertNotNull(submitted.getResponseId());
        Assertions.assertEquals(42L,submitted.getUserId());
        Assertions.assertEquals(4,submitted.getAnswers().get("satisfied"));
        Assertions.assertEquals(5,submitted.getAnswers().get("recommend"));
        Assertions.assertEquals("Works well",submitted.getComment());
        Assertions.assertEquals(SurveyResponseStatus.NEW,submitted.getStatus());
        Assertions.assertNull(submitted.getSent());
        Assertions.assertNotNull(submitted.getCreated());
        Assertions.assertNotNull(submitted.getUpdated());
    }

    @Test
    public void
    testSubmitRejectsMissingAnswer()
    {
        assertRejected(Map.of("satisfied",4,"needs",3),null);
    }

    @Test
    public void
    testSubmitRejectsOutOfRangeAnswer()
    {
        assertRejected(Map.of("satisfied",0,"needs",3,"recommend",5),null);
        assertRejected(Map.of("satisfied",6,"needs",3,"recommend",5),null);
    }

    @Test
    public void
    testSubmitRejectsUnknownQuestion()
    {
        assertRejected(Map.of("satisfied",4,"needs",3,"recommend",5,"other",1),null);
    }

    @Test
    public void
    testSubmitRejectsLongComment()
    {
        assertRejected(Map.of("satisfied",4,"needs",3,"recommend",5),"x".repeat(GraniteSurveyQuestions.MAX_COMMENT_LENGTH + 1));
    }

    @Test
    public void
    testUpdateSurveyResponseStatusAndNotes()
    {
        SurveyResponseData submitted = submit(Map.of("satisfied",2,"needs",2,"recommend",1),null);

        UpdateSurveyResponsesReply reply =
            getCommandSubject()
                .updateSurveyResponses(
                    new UpdateSurveyResponsesRequest()
                        .setResponses(
                            List.of(
                                new UpdateSurveyResponseData()
                                    .setResponseId(submitted.getResponseId())
                                    .setStatus(SurveyResponseStatus.RECEIVED)
                                    .setNotes("Followed up"),
                                new UpdateSurveyResponseData()
                                    .setResponseId(-1L)
                                    .setNotes("unknown"))))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertEquals(Set.of(-1L),reply.getFailedResponseIds());
        Assertions.assertEquals(1,reply.getUpdatedResponses().size());

        SurveyResponseData updated = reply.getUpdatedResponses().get(0);

        Assertions.assertEquals(SurveyResponseStatus.RECEIVED,updated.getStatus());
        Assertions.assertEquals("Followed up",updated.getNotes());
        Assertions.assertNotNull(updated.getSent());
        Assertions.assertNotNull(updated.getReceived());
    }

    @Test
    public void
    testMarkSentThenReceived()
    {
        SurveyResponseData submitted = submit(Map.of("satisfied",5,"needs",5,"recommend",5),null);
        Set<Long>          ids       = Set.of(submitted.getResponseId());

        MarkSurveyResponsesSentReply sent =
            getCommandSubject()
                .markSurveyResponsesSent(new MarkSurveyResponsesSentRequest().setResponseIds(ids))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(sent.isSuccess(),sent.getFailureMessage());
        Assertions.assertEquals(SurveyResponseStatus.SENT,sent.getUpdatedResponses().get(0).getStatus());

        MarkSurveyResponsesSentReply again =
            getCommandSubject()
                .markSurveyResponsesSent(new MarkSurveyResponsesSentRequest().setResponseIds(ids))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(again.isSuccess());
        Assertions.assertEquals(ids,again.getFailedResponseIds());

        MarkSurveyResponsesReceivedReply received =
            getCommandSubject()
                .markSurveyResponsesReceived(new MarkSurveyResponsesReceivedRequest().setResponseIds(ids))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(received.isSuccess(),received.getFailureMessage());
        Assertions.assertEquals(SurveyResponseStatus.RECEIVED,received.getUpdatedResponses().get(0).getStatus());
        Assertions.assertNotNull(received.getUpdatedResponses().get(0).getReceived());
    }

    @Test
    public void
    testMarkReceivedRequiresSent()
    {
        SurveyResponseData submitted = submit(Map.of("satisfied",3,"needs",3,"recommend",3),null);

        MarkSurveyResponsesReceivedReply reply =
            getCommandSubject()
                .markSurveyResponsesReceived(new MarkSurveyResponsesReceivedRequest().setResponseIds(Set.of(submitted.getResponseId())))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertEquals(Set.of(submitted.getResponseId()),reply.getFailedResponseIds());
    }

    @Test
    public void
    testFindSurveyResponses()
    {
        SurveyResponseData submitted = submit(Map.of("satisfied",4,"needs",4,"recommend",4),null);

        FindSurveyResponsesByIdsReply byIds =
            getQuerySubject()
                .findSurveyResponsesByIds(new FindSurveyResponsesByIdsRequest().setResponseIds(Set.of(submitted.getResponseId())))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(byIds.isSuccess(),byIds.getFailureMessage());
        Assertions.assertEquals(submitted.getResponseId(),byIds.getResponses().get(0).getResponseId());

        FindAllSurveyResponsesReply open =
            getQuerySubject()
                .findAllSurveyResponses(new FindAllSurveyResponsesRequest().setStatuses(Set.of(SurveyResponseStatus.NEW)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(open.isSuccess(),open.getFailureMessage());
        Assertions.assertTrue(open.getResponses().stream().anyMatch(r -> r.getResponseId().equals(submitted.getResponseId())));
        Assertions.assertTrue(open.getResponses().stream().allMatch(r -> r.getStatus() == SurveyResponseStatus.NEW));
    }

    protected abstract ISurveyCommandService
    getCommandSubject();

    protected abstract ISurveyQueryService
    getQuerySubject();

    protected SurveyResponseData
    submit(Map<String,Integer> answers,String comment)
    {
        SubmitSurveyResponseReply reply = doSubmit(answers,comment);

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getSubmittedResponse());
        return reply.getSubmittedResponse();
    }

    protected void
    assertRejected(Map<String,Integer> answers,String comment)
    {
        SubmitSurveyResponseReply reply = doSubmit(answers,comment);

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertNull(reply.getSubmittedResponse());
    }

    private SubmitSurveyResponseReply
    doSubmit(Map<String,Integer> answers,String comment)
    {
        return
            getCommandSubject()
                .submitSurveyResponse(
                    new SubmitSurveyResponseRequest()
                        .setResponse(
                            new SubmitSurveyResponseData()
                                .setUserId(42L)
                                .setAnswers(answers)
                                .setComment(comment)))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
