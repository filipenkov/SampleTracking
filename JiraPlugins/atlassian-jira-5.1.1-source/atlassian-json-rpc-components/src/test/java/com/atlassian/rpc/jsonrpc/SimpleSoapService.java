package com.atlassian.rpc.jsonrpc;

/**
 */
public interface SimpleSoapService
{
    String getCheese();

    String getExceptionalCheese() throws Exception;

    String getCheeseByName(String cheeseName);

    String getOverloadedCheese();

    String getOverloadedCheese(String cheeseName);

    String getOverloadedCheese(String cheeseName, long id);

    String getClashingCheese(long id, String cheeseName);

    String getClashingCheese(String id, long cheeseName);
}
