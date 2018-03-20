package com.duanyy.media.player;

/**
 * Created by duanyy on 2018/3/19.
 */

public interface IPlayer {

    void setDataSource(String dataSource);

    void play();
    void pause();
    void resume();

}
