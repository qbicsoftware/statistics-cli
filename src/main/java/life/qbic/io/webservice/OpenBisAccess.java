package life.qbic.io.webservice;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author fhanssen
 * Creates access to openBis by generatin the sessionToken.
 */
public class OpenBisAccess {

    private static final Logger logger = LogManager.getLogger(OpenBisAccess.class);

    private final String URL;
    private final int TIMEOUT = 10000;
    private final IApplicationServerApi v3;
    private final String user;
    private final String password;

    private String sessionToken = "";

    public OpenBisAccess(String url, String user, String password) {
        this.user = user;
        this.password = password;
        this.URL = url +  IApplicationServerApi.SERVICE_URL;

        v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);
        login();
        logger.info("Login to OpenBis");
    }

    private void login() {
        sessionToken = (v3.login(user, password));
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public IApplicationServerApi getV3() {
        return v3;
    }

    public void logout() {
        v3.logout(sessionToken);
    }
}
