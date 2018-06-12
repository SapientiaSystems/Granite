using Granite.ServerPlatform.WebApi;
using System.Web.Http;
using System.Web.Http.Cors;

namespace Granite.Server
{
    public static class WebApiConfig
    {
        public static void Register(HttpConfiguration config)
        {
            config.EnableCors(
                new EnableCorsAttribute("*","*","*")
                    { SupportsCredentials = true });
            config
                .Filters
                .Add(new RequireHttps());

            config.MapHttpAttributeRoutes();

            config.Routes.MapHttpRoute(
                name: "DefaultApi",
                routeTemplate: "api/{controller}/{id}",
                defaults: new { id = RouteParameter.Optional } );
        }
    }
}
