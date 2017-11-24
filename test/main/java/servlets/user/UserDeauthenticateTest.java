package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.User;
import main.java.models.meta.Msg;
import main.java.utils.DBFeeder;
import main.java.utils.JsonResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserDeauthenticateTest extends AbstractDBTest {
    @Test
    public void signed_in_user_signs_out() throws Exception {
        int userId = DBFeeder.createDummyUser().id;

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(userId).listen();
        new UserDeauthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        Msg outputMsg = new Gson().fromJson(stubber.gathered(), Msg.class);

        assertEquals(-1, stubber.authenticated());
        assertEquals(JsonResponse.OK, outputMsg.msg);
    }

    @Test
    public void signed_out_user_signs_out() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new UserDeauthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        Msg outputMsg = new Gson().fromJson(stubber.gathered(), Msg.class);

        assertEquals(-1, stubber.authenticated());
        assertEquals(JsonResponse.OK, outputMsg.msg);
    }

}