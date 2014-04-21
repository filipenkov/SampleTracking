package com.atlassian.jira.webtests;

/**
 * This containms all the known license keys for testing.  Add new ones here not in your tests!
 *
 * @since v4.0
 */
public class LicenseKeys
{
    /**
     * These are the older Licensing Version 1 objects. ***************************************************************
     */
    private static final String V1_ENTERPRISE_KEY = "NQrPoNOBNRHnXHjaFJNPxwrQoropKmMGGTamaPMdptjcGW\n"
            + "mi2KouuJLPf6fVkEq<hggtmF2K6LwzkgD3KEWKGzmc3rLm\n"
            + "QmRSsRRNrponOQoPMNwwqOmmPNQqmRmmQXXsSVUvsVXuos\n"
            + "tUUoomsotmummmmmUUoomsotmummmmmUU1qiXppfXk";

    public static final License V1_ENTERPRISE = new License(V1_ENTERPRISE_KEY, "JIRA Enterprise: Commercial Server", "Atlassian", null, -1);

    /**
     * These are the newer Licensing Version 2 objects. **************************************************************
     */

    public static final License V2_EVAL_EXPIRED = new License("AAABBA0ODAoPeNpdj9FrgzAQxt/zVwT27Ei67SGFPLQaNlnVorYdfbvZW5ehqSRR1v9+rjqEwcHB3\n"
            + "Xe/77u7A57oqjtTLigTS/6wZE9UFSVdMCZIhK6yuvX6YqT6brUdxKqHuoPfEQ0vTYO20lDT/YJud\n"
            + "IXGIUm75h1t9rFzaJ0MOAkt3vQReJQDlwWMD0W+tIX76Uqd9OiSlirf5nGhyGwkve1wlEPldY/jI\n"
            + "AFtPBowFd7CXUcDIcRkkNkzGO1GxsrX4JwGQwq0Pdo4kutnUQZvu/1j8Ho8vgRrxg+kUKks0fmhk\n"
            + "79sM5zP8GlZXltMoUEZZkmi8jBebci2s9UnOPz/8A9s3HaNMCwCFHP0uqLCN4m+CowCLwAYeBAZ8\n"
            + "qUkAhRWXaw3VS7+aedtyMy2AxFMgHrwZA==X02dd",

            "JIRA Enterprise: Evaluation", "Atlassian", "TestSEN", -1);

    public static final License V2_COMMERCIAL = new License("AAABBA0ODAoPeNptj1FPgzAUhd/7K5r4jGmZmkDSBwaNogMWYM4svtTuOmtGR27L4v69bOiL8ek+n\n"
            + "JzvO/dqDVuaDDvKbim/i2cs5iGVTUtDxiKSgdNoem8OVjzmdUKl9YA9GgevMU0PXQeojdrTBvAIS\n"
            + "MqhewOs3lcO0ImAkxRBnduZ8iBCNosCHgaMk0+D6nphNFgHcmsuAlm2sl7WeSOnWGlvjiA8DkAKZ\n"
            + "UazVVaD/OoNniZgFEUj7QyscKescReZSPxeOWeUJdOuPBPz+6gNXlbPN8HTZvMQzBlfk0aWogXnx"
            + "0t+t/wL/wnbUw+l6kCkVVHIOs2TBVkOqD+Ug78PfgPSNG6BMC0CFBZN4P8f7FRdIKPx3oWYkJwGf\n"
            + "M9tAhUAllLN8BrljKaWmkSeRtRCiwHCZ7g=X02dd",

            "JIRA Enterprise: Commercial Server", "Atlassian", "TestSEN", -1);

    public static final License V2_PERSONAL = new License("AAAA/w0ODAoPeNptj0FLw0AQhe/7KxY8R3abqCSwh9QsGq1JSFIrxcuYjnWl3YbZTbH/3rTViwgDD\n"
            + "+Yx33tzscAVT4c1F1dcXidhlEjJddPyiRAxy9B1ZHpvdlY95HXKtfVIPRmHrwmvkNzOwoYVw/YNq\n"
            + "Xyfu3GjQnZLCMebDDyqiZBRIG4CGbJPQ3A5Mx1ah3plTlhdtLqu6rzRZxs6b/aoPA3InsCMeRZsh\n"
            + "/qrN3Q4A+M4DoQch5W0BmvcKUylfgPOGbCsQdoj5Zma3sVt8DJ/joLH5fI+mAq5YI0uVIvOj8p+u\n"
            + "/wL/zHbQ48FbFFVum7KIp2xaqDuAxz+fe8bfzFqkjAsAhRd3AFNtzEiACcYVu8u5HnrRO68YAIUZ\n"
            + "CQ3GUWe5e7zb6bxCLJ87vkPvow=X02d5",

            "JIRA Enterprise: Personal", "Atlassian", "TestSEN", 3);

    public static final License V2_STARTER = new License("AAAA/Q0ODAoPeNptj0trwzAQhO/6FYKeXawmKdigg1OL1n04xlKaEnrZOttUJVHNSg7Nv6/y6KUUF\n"
            + "pZl2G9mLha44sWw5umEi+t8NM6F4EobfpWmGSvRd2T7YL+cvK/agisXkHqyHl9zrgNQPFk9bN+QZ\n"
            + "u9zj+TlhN0QwuGlhIDywEnSUSJS9mkJLh9th86jWtkjVdVGtU1baXWSoQt2hzLQgOwJbLRz4DpU3\n"
            + "72l/QmYZREo4rAZrcFZfzSTJdjNnjcbcBiYRtohVaWc3mYmeZk/j5OH5fIumaZiwbSqpUEf4ma/c\n"
            + "f7ln0Wz77GGLUptijbGZc1A3Qd4/FvwB9K7ascwLAIUSYbbyUppaa3vYFk4u2DWR2T7M6sCFDmM8\n"
            + "zC2zNlX3A4hc0IGWZKGm6mVX02d1",

            "JIRA Enterprise: Starter", "Daily Planet", "TestSEN", 5);

    public static final License V2_COMMUNITY = new License("AAABAg0ODAoPeNptj81OwzAQhO9+Ckucjew2gBLJhzSxIECSKj8UKi4mXYoRcaO1U9G3J7RwQUgj7\n"
            + "WE03+ycrWBD43FL+QUVl9E8iISgqm7ojPOQpOA6NIM3OytvsyqmynrAAY2D54gmu74frfEHUoz9C\n"
            + "2D52jpAJ5kgCYL+TqXag5xxETB+xcScvBvU5/emA+tAbcwRrIpGVcsqq9XJ1p03e5AeRyC5NlOj1\n"
            + "bYD9TkYPJyAYRgyLiaRErfaGncsk7H/0M4ZbUkNuAfMUrm4Dhv22D4E7G69vmELLlakVoVswPnpk\n"
            + "t9f/oX/mM1hgEL3IJMyz9sia57IcsTuTTv4u+8LzGFrnzAsAhQzw+box0+SRcram+LNL1QM9sWIy\n"
            + "QIUXKMPl3l3z7xzMOYkWrZtPoq/rsw=X02d9",

            "JIRA Enterprise: Community", "Atlassian", "TestSEN", -1);

    public static final License V2_OPEN_SOURCE = new License("AAABBA0ODAoPeNptj19LwzAUxd/zKQI+R5KtKi3koVuD1j9taVonQ5DYXWfEZeUmHe7br276IsKB+\n"
            + "3C4v3PO2QJWNB3WlF9QcZlMo0QIqnRDJ5zHJAPfoe2D3Tp5m9cpVS4A9mg9PCe07MFRvR2wA1IMm\n"
            + "1fA8q31gF4yQeYI5vsvMwHkhIuI8SsmpuTDojm/tx04D2plj2hVNKqu6lyrk226YHcgAw5AHowdM\n"
            + "51xHaiv3uL+BIzjmHExipS4Ns76Y5hMw6fx3hpHNOAOMM/k7Dpu2FP7GLG75fKGzbhYEK0K2YAP4\n"
            + "yW/Xf6F/5jNvofCbECWlSpedNnWc0Wqcfi78fB34QEyhWyaMCwCFCV/1L+kSN8+pbg+VL6eG1OKy\n"
            + "5vxAhQDr+5XmUh60ciFHrgMOEd4KBg7HQ==X02dd",

            "JIRA Enterprise: Open Source", "Atlassian", "TestSEN", -1);

    public static final License V2_DEVELOPER = new License("AAABCw0ODAoPeNptj0FPwkAQhe/7KzbxvGYXS0hJ9lDsRlEspC1giJexDLimLM3stpF/L1C9GJNJ5\n"
            + "vDmfe/NzRq3PGn3XA65Go1lNFZDboqSD6SMWYq+ItsEe3T6aZon3LiA1JD1+DbmKXZYHxsklrWHd\n"
            + "6T5bumRvBaK3RPCxZVCQD2QKhJyJNQd+7QEtzNbofNotvYKNllp8kU+LQwzHdTt1ah3UHvs76EKt\n"
            + "kMdqEX2AvZcwYGr0Hw1lk59QhzHQqrzsDntwVnfQ5JQg/cWHCuQOqRpqicPcSlel6tIPG82j2Ii1\n"
            + "ZoVJtMl+nDe7Lfcv/AfsTw1mMEBdWpWZjZfmJwtWqo+wOPfh78Bti5xzTAsAhRAOSQxvzuXgT4Mp\n"
            + "0jxmjPeUwn2GAIUL+RJPR39dtEJobuKhAMjnXBgCGc=X02dl",

            "JIRA Enterprise: Developer", "Atlassian", "TestSEN", -1);

    public static final License V2_DEMO = new License("AAABAw0ODAoPeNptj1FLwzAUhd/zKwI+R5KtKi3kobNBqy4dbeZk+BK764zYrNykw/1766YvIlw4D\n"
            + "x9859yzFWxoPmwpv6DiMpsmmRBUNYZOOE9JAaFF10e38/KurHOqfATs0QV4zmgB3c6HiPabEz10L\n"
            + "4DV6zIABskEuUY4ksJGkBMuEsavmJiSd4f2/MG14AOojTvKlTaqXtRlo07YttHtQUYcgMytG1u99\n"
            + "S2oz97h4SRM05RxMR6pcGu9C8cymccPG4KznjSAe8CykLOb1LCn5WPC7tfrWzbjYkUapaWBEMckv\n"
            + "1v+lf9Ac+hB2w5koeaVbkydm7LSZDFg+2YD/P3xC5BLbmMwLAIUF0/smkqvJQIc+DW1STvRHCa7P\n"
            + "HUCFE4CE4EDF58jmSnA0GCmQ2YYdaFaX02d9",

            "JIRA Enterprise: Demonstration", "Atlassian", "TestSEN", -1);

    public static final License V2_HOSTED = new License("AAABAw0ODAoPeNptj09rwzAMxe/+FIadM5ytHSTgQ7qYJfuTlMTdRtlFS9XWI3WD7JT12y9tussYC\n"
            + "ASS3u89Xeltz5N+w8UdD6fxJIxvp1zVmt8IEbEUXUOm82Zv5WNeJVxZj9SRcfgR82zvPK5Y0e8+k\n"
            + "cr1wiE5OejYPSGcNCl4PA1EIMKh2JchuH42DVqHamXOWFVoVc2rvFZMHaDtz0K5htbheA+NNweUn\n"
            + "npkL2CGABZsg+q7M3QcHaIoujiUtAFr3AhJfAvOGbCsRjog5amcPUQ6eF+8ToKn5TILZiJ8Y7Uqp\n"
            + "Ebnh85+w/0Lvyz1scMCdiizstYqZfOemi04/PvtDyEab9owLAIUe/w4660z/11rTannGUCT5tY9F\n"
            + "QYCFDZedzZxTm1SjWpeQSA1B3AzJnH6X02d9",

            "JIRA Enterprise: Hosted", "Atlassian", "TestSEN", 200);

    public static final License V2_DEVELOPER_LIMITED = new License("AAABDg0ODAoPeNpdj1FrgzAUhd/zKwJ7dqS6wSrkwc6wurUqartR9pLZW5dhY7iJsv77tZZCu6fL4\n"
            + "XK+c87dstM06hvKAjrxwyAIfZ+KsqI+Y1MSg61RGac6zSuwjraqBm2B7jqkpu0bpekWBmg7A2hJ2\n"
            + "u+/ALPdyh4VfyTPCPLkjaUDfgJ67MljAflRKO8XZ5TYqhEv0koUeZGU4vyWtVMDcIc9kKVU2oGWu\n"
            + "gbxaxQeRmAezEmGjdTKjik8cq20VklNSsABMIn57GVaeR+r9YP3ttnMvRmbvJNSpOOY471pUh0M8\n"
            + "NekiKg4pqFBZeEzpPFlH7k0vq1w5U7lHngs1mKR5aIgeY/1t7Twf/4fRyp8ezAtAhR2RgZb5a98I\n"
            + "4hExn2aanmpAxTLrwIVAIw1YU2Dns+O9mjEsQqh7hCMe67gX02dp",

            "Test license for plugin developers", "Atlassian", "TestSEN", 5);

    public static final License V2_COMMERCIAL_LIMITED = new License("AAABCA0ODAoPeNptj0Frg0AQhe/7KxZ6tqwhoWxgD0aXVho1qElLblMzSbfoKrurNP++xthLKQzMw\n"
            + "Jv3vZmHpNU06C/Uf6I+W/t8veRUFiVdMMZJhLYyqnOq1SJsmwZNpaCmK7q3aOhWNcrhiR4W41iht\n"
            + "kjSvvlAk51vuhUrEhqEmzsCh2JEMo/5Y5EvZeBxNsmTmgJkWsp8l8eFJHKAup+M4gz1yJ32oXJqQ\n"
            + "OFMjyQBpR1q0BXK706Z6z2Bcz4nZOYCWtk7JHA1WKtAkwLNgCaOxOaZl977/rD0Xo/HF2/D/DdSy\n"
            + "FSUaN3Yye9x/8Jnsbx2mEKDIsySROZhHGzJrjfVJ1j8+/EPxuN0kTAsAhQI2iz1si4iwU9hUc4Pf\n"
            + "nyjlStYrQIUMGzgIo/msW60dp6g4v59ApVJZ+0=X02dh",

            "Commercial 5 User Limited V2 License", "Atlassian", "TestSEN", 5);

    /**
     * For testing purpose you can use this MAINTENANCE EXPIRED COMMERCIAL LICENSE
     *
     * However you cant put it in a func test because JIRA will lock up with a Johnson Event!
     *
AAABNA0ODAoPeNpVkE9vgzAMxe/5FJF2pqKM/lmlSCuQA1uhFUWdJu3ipV6bCQJKAlq//SgUdZN9s
9/7PfshqRRdNyf6OKWet5r5K39Jwyinnus+kQiN0LK2slLsJc7WlCuLutbS4MeKhlVZohYSCrpH3
aImoUa4LkdgkV0dnGtPSVgpC8KmUCI7IKqGBmfQXZFvqWGykQKVQX6UPYmnOc92Wbzno5AnIAvWf
g6iZ7AFGCNBTURVEt5C0fRY9gWFwX+e+aXGHhtuk4RnYbzeDPPOVbbIrG6QdO7dXQqUQP5TS335k
3/peC7Z6hMoaQZIjsbSfg+P9IYhe56yqTtfzHyfDM+IIxbk6dw5uO+58xbywAleZztyE3TTTRyNi
vEBd3ijCllKi0eya7Q4g8Ex0+KW6RcTBpI3MCwCFEo+rTahLjs5IQIjIS2OGtr65XncAhRIqzT82
vXSzz/u9sheljEfB4G2JQ==X02ff

     */

    public static class License
    {
        private final String licenseString;
        private final String description;
        private final String organisation;
        private final String sen;
        private final int maxUsers;

        public License(final String licenseString, final String description, final String organisation, final String sen, final int maxUsers)
        {
            this.licenseString = licenseString;
            this.description = description;
            this.organisation = organisation;
            this.sen = sen;
            this.maxUsers = maxUsers;
        }

        public String getLicenseString()
        {
            return nvl(licenseString);
        }

        public String getDescription()
        {
            return nvl(description);
        }

        public String getOrganisation()
        {
            return nvl(organisation);
        }

        public String getSen()
        {
            return nvl(sen);
        }

        public int getMaxUsers()
        {
            return maxUsers;
        }

        private String nvl(String s)
        {
            return s == null ? "" : s;
        }
    }


/*
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
    To use this code to generate a license, add a dependency on atlassian-extras-encoder (same version as atlassian-extras)
    Update the properties you need and run.

    https://maven.atlassian.com/browse/com.atlassian.extras/atlassian-extras-encoder

    !!!!!!!!!!!!! Do no leave the encoder dependency in, as it MUST NOT ship with products. !!!!!!!!!!!!!
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
*/

    /*
    public static void main(String[] args)
    {
        generateExpiredEvalLicenses();
    }

    private static void generateExpiredEvalLicenses()
    {
        generateLicense("Expired Evaluation Commercial V2 License", "1999-01-01", "2999-01-01", LicenseType.COMMERCIAL, true, -1);
    }

    private static void generateExpiredLicenses()
    {
        generateLicense("Expired Commercial V2 License", "1999-01-01", "2999-01-01", LicenseType.COMMERCIAL, false, -1);
        generateLicense("Expired Community V2 License", "1999-01-01", "2999-01-01", LicenseType.COMMUNITY, false, -1);
        generateLicense("Expired Demo V2 License", "1999-01-01", "2999-01-01", LicenseType.DEMONSTRATION, false, -1);
    }

    private static void generateExpiredMainteanceLicenses()
    {
        generateLicense("Maintenance Expired Commercial V2 License", "2999-01-01", "1999-01-01", LicenseType.COMMERCIAL, false, -1);
        generateLicense("Maintenance Expired Community V2 License", "2999-01-01", "1999-01-01", LicenseType.COMMUNITY, false, -1);
        generateLicense("Maintenance Expired Demo V2 License", "2999-01-01", "1999-01-01", LicenseType.DEMONSTRATION, false, -1);
    }

    private static void generateLicenseOpenEndedLimitedLic()
    {
        generateLicense("Commercial 5 User Limited V2 License", "2999-01-01", "2999-01-01", LicenseType.COMMERCIAL, false, 5);
    }

    private static void generateLicenseOpenEndedLic()
    {
        generateLicense("Some license description", "2999-01-01", "2999-01-01", LicenseType.COMMERCIAL, false, 5);
    }


    private static void generateLicense(final String licenseDesc, final String expiryDate, final String maintenanceDate, final LicenseType licenseType, final boolean evaluation, final int maxNumberOfUsers)
    {
        final AtlassianLicenseCreator licenseCreator = new AtlassianLicenseCreator(new Version2LicenseEncoder());
        licenseCreator.serverId("BG9T-XUV4-KZZH-B01W");
        licenseCreator.supportEntitlementNumber("TestSEN");
        licenseCreator.creationDate("2000-01-01");
        licenseCreator.purchaseDate("2000-01-01");
        licenseCreator.expiryDate(expiryDate);
        licenseCreator.maintenanceExpiryDate(maintenanceDate);
        licenseCreator.maximumNumberOfUsers(maxNumberOfUsers);
        licenseCreator.organisation("Atlassian");
        licenseCreator.licenseType(licenseType);
        licenseCreator.description(licenseDesc);
        licenseCreator.evaluation(evaluation);
        licenseCreator.product(new DefaultJiraLicenseBuilder().activate(true).licenseEdition(LicenseEdition.ENTERPRISE));

        final String newLicense = licenseCreator.create();

        printLicense(getJiraLicense(newLicense));

        System.out.println();
        System.out.println(newLicense);
        System.out.println();


        System.out.println("----------------------");
    }

    private static JiraLicense getJiraLicense(final String key)
    {
        return (JiraLicense) LicenseManagerFactory.getLicenseManager().getLicense(key).getProductLicense(Product.JIRA);
    }

    private static void printLicense(final JiraLicense license)
    {
        System.out.println("Type: " + license.getLicenseType());
        System.out.println("Desc: " + license.getDescription());
        System.out.println("Creation date: " + license.getCreationDate());
        System.out.println("Purchase date: " + license.getPurchaseDate());
        System.out.println("Maintenance exp: " + license.getMaintenanceExpiryDate());
        System.out.println("Expiry: " + license.getExpiryDate());
        System.out.println("Eval: " + license.isEvaluation());
        System.out.println("Users: " + license.getMaximumNumberOfUsers());
        System.out.println("Org: " + license.getOrganisation().getName());
        System.out.println("Edition: " + license.getLicenseEdition());
        System.out.println("SEN: " + license.getSupportEntitlementNumber());
    }
    */
}
