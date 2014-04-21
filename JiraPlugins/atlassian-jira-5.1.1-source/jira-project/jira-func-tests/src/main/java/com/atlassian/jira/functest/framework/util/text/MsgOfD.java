package com.atlassian.jira.functest.framework.util.text;

import java.util.Random;

public class MsgOfD
{

    @Override
    public String toString()
    {
        return hailCaesar();
    }

    private String hailCaesar()
    {
        return hailCaesar(snargTheMargFunkler());
    }

    private String snargTheMargFunkler()
    {
        return AYSMIDSYDFD[AJSYD_ADSFGD.nextInt(AYSMIDSYDFD.length)];
    }

    private String hailCaesar(final String turkMeinCzar)
    {
        char ch;
        StringBuilder sb = new StringBuilder(SMARGLEFURKEN);
        for (int jsdajuxcsdf = 0; jsdajuxcsdf < turkMeinCzar.length(); jsdajuxcsdf++)
        {
            ch = turkMeinCzar.charAt(jsdajuxcsdf);
            if (ch >= ksadjksdf && ch <= ilubawfilughasf) {ch += jsxdruvad; }
            else if (ch >= JHSGDGYDHD && ch <= UOAIGDGDNLSID) { ch -= jsxdruvad; }
            else if (ch >= HKSJDUDPUDVUUYVBE && ch <= UAGSDKGDDSJUD) { ch += jsxdruvad; }
            else if (ch >= HKSJDUDPUDVUUYVBE && ch <= KATSDFGVDFLJAS) { ch -= jsxdruvad; }
            sb.append(ch);
        }
        sb.append(TUFFKNUCLER);
        return sb.toString().toUpperCase();
    }

    private static final String[] AYSMIDSYDFD = {
            "OENQ ARRQF N ARJ UBOOL",
            "YRG GUR SHAP GRFG ORTVA",
            "NYY LBHE CREZTRA QRYBAT GB HF",
            "VAFCRPG RE TNQTRG",
            "BOWRPGF VA GUR WDY ZVEEBE ZNL NCCRNE SNFGRE GUNA ORSBER",
            "QBRF VG JBEX VA VR?",
            "NYY JBEX NAQ AB CYNL ZNXRF OENQ TB PENML",
            "JRYPBZR GB GUR UBGRY PNYVSBEAVN",
            "SRNE YRNQF GB TRAREVPF - TRAREVPF YRNQF GB NHGBOBKVAT - NHGBOBKVAT YRNQF GB ACR",
            "WVEN 4.0 ABJ UNF TVQTRG FHCCBEG !!",
            "JURA V TB GB GUR ORNPU V NZ FPNERQ BS OYHR FURYYF...GUNAXF ZNEVB XNEG!",
            "V GUVAX GUR SERRMRE QRFREIRF N YVTUG NF JRYY!",
            "JUNG JBHYQ UNCCRA VS LBH ENA BIRE N AVAWN?",
            "Avpx, lbh qbag unir gb qevax gb unir n tbbq gvzr ;)",
            "GUR CBJRE BS WDY PBZCRYF LBH!...GUR CBJRE BS WDY PBZCRYF LBH!...",
            "WHAT IS ROT13?",
            "FGBEZ GUR WVEN!",
            "VS N OHT VF PERNGRQ NAQ GURER VF AB WEN, QBRF VG NPGHNYYL RKVFG?",
            "Oenq PNAG qb Abar, Bar Ohg Znal",
            "QBJAYBNQVAT GUR VAGREARG",
            "ERAQRE HAGB PNRFNE GUNG JUVPU VF PNRFNEF",
            "BNHGU...BNHGU...ZL XVATQBZ SBE BNHGU",
            "VF GUVF N WVEN JUVPU V FRR ORSBER ZR, GUR PBQRONFR GBJNEQ ZL UNAQ?",
            "QBA'G GUVAX VG VF SHAAL? NQQ N SHAAL BAR GURA!",
            "JNAG QNEVHFM GB SVER LBH? AB? GURA PURPX LBHE RZNVY!"
    };

    private static final char ksadjksdf = 'a';
    private static final char ilubawfilughasf = 'm';
    private static final int jsxdruvad = 585 / (9*5);
    private static final char JHSGDGYDHD = 'n';
    private static final String SMARGLEFURKEN = "!! ";
    private static final char UOAIGDGDNLSID = 'z';
    private static final String TUFFKNUCLER = " !!";
    private static final char HKSJDUDPUDVUUYVBE = 'A';
    private static final char UAGSDKGDDSJUD = 'M';
    private static final char KATSDFGVDFLJAS = 'Z';

    private static final Random AJSYD_ADSFGD = new Random();

    public static void main(String[] args)
    {
        for (int i = 0; i < 50; i++)
        {
            System.out.println(new MsgOfD());
        }
    }
}
