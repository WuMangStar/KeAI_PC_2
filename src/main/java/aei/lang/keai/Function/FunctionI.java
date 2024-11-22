package aei.lang.keai.Function;

import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;

public interface FunctionI {
    String getName();

    void init(SecPlugin api, Messenger messenger, Connection conn) throws Exception;

}
