//  ##########################################################################
//  # File Name: ActiveDirectoryGraphClient.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Microsoft.IdentityModel.Clients.ActiveDirectory;
using Newtonsoft.Json;
using RestSharp;
using RestSharp.Authenticators;
using System.Net;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ActiveDirectoryGraphClient
    {
        private string clientId;
        private string clientSecret;
        private string tenantId;

        private string baseUrl;

        private AuthenticationContext authContext;
        private ClientCredential      credential;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ActiveDirectoryGraphClient(
            string clientId,
            string clientSecret,
            string tenantId)
        {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.tenantId = tenantId;
            baseUrl = Globals.AadGraphEndpoint + tenantId;

            this.authContext = 
                new AuthenticationContext(
                    "https://login.microsoftonline.com/" + tenantId);

            this.credential = new ClientCredential(clientId,clientSecret);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        CreateUser(string json)
        {
            return SendGraphPostRequest("/users",json);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        UpdateUser(string objectId,string json)
        {
            return SendGraphPatchRequest("/users/" + objectId,json);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        DeleteUser(string objectId)
        {
            return SendGraphDeleteRequest("/users/" + objectId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string
        GetUserByObjectId(string objectId)
        {
            return SendGraphGetRequest("/users/" + objectId,null);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string
        GetAllUsers(string query)
        {
            return SendGraphGetRequest("/users",query);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        RegisterExtension(string objectId,string body)
        {
            return 
                SendGraphPostRequest(
                    "/applications/" + objectId + "/extensionProperties",
                    body);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        UnregisterExtension(string appObjectId,string extensionObjectId)
        {
            return 
                SendGraphDeleteRequest(
                    "/applications/" + appObjectId + 
                    "/extensionProperties/" + extensionObjectId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        GetExtensions(string appObjectId)
        {
            return 
                SendGraphGetRequest(
                    "/applications/" + appObjectId + "/extensionProperties",
                    null);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        GetApplications(string query)
        {
            return SendGraphGetRequest("/applications",query);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private string 
        SendGraphDeleteRequest(string api)
        {
            AuthenticationResult result =
                authContext.AcquireToken(
                    Globals.AadGraphResourceId,
                    credential);
            string resource = api + "?" + Globals.AadGraphVersion;

            IRestClient   http     = new RestClient(baseUrl);
            IRestRequest  request  = new RestRequest(resource,Method.DELETE);
            IRestResponse response = null;

            http.Authenticator = 
                new OAuth2AuthorizationRequestHeaderAuthenticator(
                    result.AccessToken,
                    result.AccessTokenType);

            response = http.Execute(request);

            if (!response.IsSuccessful)
            {
                string error = response.ErrorMessage;
                object formatted = JsonConvert.DeserializeObject(error);
                throw 
                    new WebException(
                        "Error Calling the Graph API: \n" + 
                        JsonConvert.SerializeObject(
                            formatted,
                            Formatting.Indented));
            }

            return response.Content;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private string
        SendGraphPatchRequest(string api,string json)
        {
            AuthenticationResult result =
                authContext.AcquireToken(
                    Globals.AadGraphResourceId,
                    credential);
            string resource = api + "?" + Globals.AadGraphVersion;

            IRestClient   http     = new RestClient(baseUrl);
            IRestRequest  request  = new RestRequest(resource,Method.PATCH);
            IRestResponse response = null;

            http.Authenticator =
                new OAuth2AuthorizationRequestHeaderAuthenticator(
                    result.AccessToken,
                    result.AccessTokenType);

            request.AddParameter(
                "application/json",
                json,
                ParameterType.RequestBody);
            response = http.Execute(request);

            if (!response.IsSuccessful)
            {
                string error = response.ErrorMessage;
                object formatted = JsonConvert.DeserializeObject(error);
                throw
                    new WebException(
                        "Error Calling the Graph API: \n" +
                        JsonConvert.SerializeObject(
                            formatted,
                            Formatting.Indented));
            }

            return response.Content;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private string 
        SendGraphPostRequest(string api,string json)
        {
            AuthenticationResult result =
                authContext.AcquireToken(
                    Globals.AadGraphResourceId,
                    credential);
            string resource = api + "?" + Globals.AadGraphVersion;

            IRestClient   http     = new RestClient(baseUrl);
            IRestRequest  request  = new RestRequest(resource,Method.POST);
            IRestResponse response = null;

            http.Authenticator =
                new OAuth2AuthorizationRequestHeaderAuthenticator(
                    result.AccessToken,
                    result.AccessTokenType);

            request.AddParameter(
                "application/json",
                json,
                ParameterType.RequestBody);
            response = http.Execute(request);

            if (!response.IsSuccessful)
            {
                string error = response.ErrorMessage;
                object formatted = JsonConvert.DeserializeObject(error);
                throw
                    new WebException(
                        "Error Calling the Graph API: \n" +
                        JsonConvert.SerializeObject(
                            formatted,
                            Formatting.Indented));
            }

            return response.Content;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public string 
        SendGraphGetRequest(string api,string query)
        {
            AuthenticationResult result =
                authContext.AcquireToken(
                    Globals.AadGraphResourceId,
                    credential);
            string resource = 
                api + "?" + Globals.AadGraphVersion + 
                (string.IsNullOrEmpty(query) ? string.Empty : "&" + query);

            IRestClient   http     = new RestClient(baseUrl);
            IRestRequest  request  = new RestRequest(resource,Method.GET);
            IRestResponse response = null;

            http.Authenticator =
                new OAuth2AuthorizationRequestHeaderAuthenticator(
                    result.AccessToken,
                    result.AccessTokenType);

            response = http.Execute(request);

            if (!response.IsSuccessful)
            {
                string error = response.ErrorMessage;
                object formatted = JsonConvert.DeserializeObject(error);
                throw
                    new WebException(
                        "Error Calling the Graph API: \n" +
                        JsonConvert.SerializeObject(
                            formatted,
                            Formatting.Indented));
            }

            return response.Content;
        }
    }
}

//  ##########################################################################
