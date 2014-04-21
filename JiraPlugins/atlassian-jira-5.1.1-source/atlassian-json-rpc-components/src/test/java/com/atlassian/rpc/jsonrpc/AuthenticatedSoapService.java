package com.atlassian.rpc.jsonrpc;

public interface AuthenticatedSoapService
{
    String getCheese(String token);

    String getCheeseByName(String token, String cheeseName);

    String getOverloadedCheese(String token);

    String getOverloadedCheese(String token, String cheeseName);

    String getOverloadedCheese(String token, String cheeseName, long id);

    String getClashingCheese(String token, long id, String cheeseName);

    String getClashingCheese(String token, String id, long cheeseName);
}
