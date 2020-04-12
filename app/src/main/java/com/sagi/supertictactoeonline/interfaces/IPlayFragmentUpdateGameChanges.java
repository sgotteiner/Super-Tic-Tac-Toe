package com.sagi.supertictactoeonline.interfaces;

import com.sagi.supertictactoeonline.entities.OnlineGame;
import com.sagi.supertictactoeonline.entities.User;

public interface IPlayFragmentUpdateGameChanges {
    void updateGameChanges(OnlineGame game);

    void onOtherPlayerConnectionEvent(boolean isConneted);

    void setOtherPlayer(User user);

    void onBackPressedInActivity();
}
