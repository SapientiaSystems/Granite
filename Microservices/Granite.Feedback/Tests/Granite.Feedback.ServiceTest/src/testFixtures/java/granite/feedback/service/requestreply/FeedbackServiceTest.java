//////////////////////////////////////////////////////////////////////////////
// FeedbackServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag("IntegrationStage")
public abstract
class FeedbackServiceTest
{
    @Test
    public void
    testCreateFeedback()
    {
        FeedbackData created =
            create(
                FeedbackType.DEFECT,
                "Save button does nothing",
                Map.of("stepsToReproduce","1. Open users","expectedBehavior","Saved","actualBehavior","Nothing"));

        Assertions.assertNotNull(created.getFeedbackId());
        Assertions.assertEquals(42L,created.getUserId());
        Assertions.assertEquals(FeedbackType.DEFECT,created.getType());
        Assertions.assertEquals(FeedbackStatus.NEW,created.getStatus());
        Assertions.assertEquals("Nothing",created.getDetails().get("actualBehavior"));
        Assertions.assertNull(created.getSent());
        Assertions.assertNotNull(created.getCreated());
        Assertions.assertNotNull(created.getUpdated());
    }

    @Test
    public void
    testCreateFeedbackRejectsBlankSummary()
    {
        CreateFeedbackReply reply =
            getCommandSubject()
                .createFeedback(
                    new CreateFeedbackRequest()
                        .setFeedback(
                            new CreateFeedbackData()
                                .setUserId(42L)
                                .setType(FeedbackType.KUDOS)
                                .setSummary("  ")))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertNull(reply.getCreatedFeedback());
    }

    @Test
    public void
    testUpdateFeedbackStatusAndNotes()
    {
        FeedbackData created = create(FeedbackType.SUGGESTION,"Dark mode for grids",Map.of());

        UpdateFeedbacksReply reply =
            getCommandSubject()
                .updateFeedbacks(
                    new UpdateFeedbacksRequest()
                        .setFeedback(
                            List.of(
                                new UpdateFeedbackData()
                                    .setFeedbackId(created.getFeedbackId())
                                    .setStatus(FeedbackStatus.RECEIVED)
                                    .setNotes("Planned for next release"),
                                new UpdateFeedbackData()
                                    .setFeedbackId(-1L)
                                    .setNotes("unknown"))))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertEquals(Set.of(-1L),reply.getFailedFeedbackIds());
        Assertions.assertEquals(1,reply.getUpdatedFeedback().size());

        FeedbackData updated = reply.getUpdatedFeedback().get(0);

        Assertions.assertEquals(FeedbackStatus.RECEIVED,updated.getStatus());
        Assertions.assertEquals("Planned for next release",updated.getNotes());
        Assertions.assertNotNull(updated.getSent());
        Assertions.assertNotNull(updated.getReceived());
    }

    @Test
    public void
    testMarkSentThenReceived()
    {
        FeedbackData created = create(FeedbackType.PROBLEM,"Slow search",Map.of("impact","Waiting"));

        MarkFeedbackSentReply sent =
            getCommandSubject()
                .markFeedbackSent(new MarkFeedbackSentRequest().setFeedbackIds(Set.of(created.getFeedbackId())))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(sent.isSuccess(),sent.getFailureMessage());
        Assertions.assertEquals(FeedbackStatus.SENT,sent.getUpdatedFeedback().get(0).getStatus());

        MarkFeedbackSentReply again =
            getCommandSubject()
                .markFeedbackSent(new MarkFeedbackSentRequest().setFeedbackIds(Set.of(created.getFeedbackId())))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(again.isSuccess());
        Assertions.assertEquals(Set.of(created.getFeedbackId()),again.getFailedFeedbackIds());

        MarkFeedbackReceivedReply received =
            getCommandSubject()
                .markFeedbackReceived(new MarkFeedbackReceivedRequest().setFeedbackIds(Set.of(created.getFeedbackId())))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(received.isSuccess(),received.getFailureMessage());
        Assertions.assertEquals(FeedbackStatus.RECEIVED,received.getUpdatedFeedback().get(0).getStatus());
        Assertions.assertNotNull(received.getUpdatedFeedback().get(0).getReceived());
    }

    @Test
    public void
    testMarkReceivedRequiresSent()
    {
        FeedbackData created = create(FeedbackType.KUDOS,"Great tool",Map.of());

        MarkFeedbackReceivedReply reply =
            getCommandSubject()
                .markFeedbackReceived(new MarkFeedbackReceivedRequest().setFeedbackIds(Set.of(created.getFeedbackId())))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertEquals(Set.of(created.getFeedbackId()),reply.getFailedFeedbackIds());
    }

    @Test
    public void
    testFindFeedback()
    {
        FeedbackData created = create(FeedbackType.DEFECT,"Typo on login",Map.of());

        FindFeedbackByIdsReply byIds =
            getQuerySubject()
                .findFeedbackByIds(new FindFeedbackByIdsRequest().setFeedbackIds(Set.of(created.getFeedbackId())))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(byIds.isSuccess(),byIds.getFailureMessage());
        Assertions.assertEquals(created.getFeedbackId(),byIds.getFeedback().get(0).getFeedbackId());

        FindAllFeedbackReply open =
            getQuerySubject()
                .findAllFeedback(new FindAllFeedbackRequest().setStatuses(Set.of(FeedbackStatus.NEW)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(open.isSuccess(),open.getFailureMessage());
        Assertions.assertTrue(open.getFeedback().stream().anyMatch(f -> f.getFeedbackId().equals(created.getFeedbackId())));
        Assertions.assertTrue(open.getFeedback().stream().allMatch(f -> f.getStatus() == FeedbackStatus.NEW));
    }

    protected abstract IFeedbackCommandService
    getCommandSubject();

    protected abstract IFeedbackQueryService
    getQuerySubject();

    protected FeedbackData
    create(FeedbackType type,String summary,Map<String,String> details)
    {
        CreateFeedbackReply reply =
            getCommandSubject()
                .createFeedback(
                    new CreateFeedbackRequest()
                        .setFeedback(
                            new CreateFeedbackData()
                                .setUserId(42L)
                                .setType(type)
                                .setSummary(summary)
                                .setDetails(details)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getCreatedFeedback());
        return reply.getCreatedFeedback();
    }
}

//////////////////////////////////////////////////////////////////////////////
