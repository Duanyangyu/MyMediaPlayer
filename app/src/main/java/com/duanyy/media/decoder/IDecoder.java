package com.duanyy.media.decoder;

/**
 * Created by duanyy on 2018/3/19.
 */

public interface IDecoder {

    void setDataSource(String dataSource);

    void play();
    void pause();
    void resume();

    void release();
}
