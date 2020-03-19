package com.sagi.supertictactoeonline.interfaces;

import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.entities.User;

import java.util.ArrayList;

public interface IHomePage {
    public void setGame(OnlineGame game, boolean isRandom);

    void setFriends(ArrayList<User> friends);

    void setInvitations(ArrayList<User> invitations);
}
