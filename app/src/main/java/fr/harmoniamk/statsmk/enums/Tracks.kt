package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

enum class Cups(
    val label: Int,
    val picture: Int,
) {
    MUSHROOM(R.string.mushroom, R.drawable.mushroom),
    FLOWER(R.string.flower, R.drawable.flower),
    STAR(R.string.star, R.drawable.star),
    SPECIAL(R.string.special, R.drawable.special),
    EGG(R.string.egg, R.drawable.egg),
    TRIFORCE(R.string.triforce, R.drawable.triforce),
    SHELL(R.string.shell, R.drawable.shell),
    BANANA(R.string.banana, R.drawable.banana),
    LEAF(R.string.leaf, R.drawable.leaf),
    LIGHTNING(R.string.lightning, R.drawable.lightning),
    CROSSING(R.string.crossing, R.drawable.crossing),
    BELL(R.string.bell, R.drawable.bell),
    GOLDEN_DASH(R.string.goldendash, R.drawable.goldendash),
    LUCKY_CAT(R.string.luckycat, R.drawable.luckycat),
    TURNIP(R.string.turnip, R.drawable.turnip),
    PROPELLER(R.string.propeller, R.drawable.propeller),
    ROCK(R.string.rock, R.drawable.rock),
    MOON(R.string.moon, R.drawable.moon),
    BOOMERANG(R.string.boomerang, R.drawable.boomerang),
    FRUIT(R.string.fruit, R.drawable.fruit)
}

enum class Maps(val label: Int, val picture: Int, val cup: Cups)  {
    MKS(R.string.mks, R.drawable.mks, Cups.MUSHROOM),
    WP(R.string.wp, R.drawable.wp, Cups.MUSHROOM),
    SSC(R.string.ssc, R.drawable.ssc, Cups.MUSHROOM),
    TR(R.string.tr, R.drawable.tr, Cups.MUSHROOM),
    MC(R.string.mc, R.drawable.mc, Cups.FLOWER),
    TH(R.string.th, R.drawable.th, Cups.FLOWER),
    TM(R.string.tm, R.drawable.tm, Cups.FLOWER),
    SGF(R.string.sgf, R.drawable.sgf, Cups.FLOWER),
    SA(R.string.sa, R.drawable.sa, Cups.STAR),
    DS(R.string.ds, R.drawable.ds, Cups.STAR),
    Ed(R.string.ed, R.drawable.ed, Cups.STAR),
    MW(R.string.mw, R.drawable.mw, Cups.STAR),
    CC(R.string.cc, R.drawable.cc, Cups.SPECIAL),
    BDD(R.string.bdd, R.drawable.bdd, Cups.SPECIAL),
    BC(R.string.bc, R.drawable.bc, Cups.SPECIAL),
    RR(R.string.rr, R.drawable.rr, Cups.SPECIAL),
    dYC(R.string.dyc, R.drawable.dyc, Cups.EGG),
    dEA(R.string.dea, R.drawable.dea, Cups.EGG),
    dDD(R.string.ddd, R.drawable.ddd, Cups.EGG),
    dMC(R.string.dmc, R.drawable.dmc, Cups.EGG),
    dWGM(R.string.dwgm, R.drawable.dwgm, Cups.TRIFORCE),
    dRR(R.string.drr, R.drawable.drr, Cups.TRIFORCE),
    dIIO(R.string.diio, R.drawable.diio, Cups.TRIFORCE),
    dHC(R.string.dhc, R.drawable.dhc, Cups.TRIFORCE),
    rMMM(R.string.rmmm, R.drawable.rmmm, Cups.SHELL),
    rMC(R.string.rmc, R.drawable.rmc, Cups.SHELL),
    rCCB(R.string.rccb, R.drawable.rccb, Cups.SHELL),
    rTT(R.string.rtt, R.drawable.rtt, Cups.SHELL),
    rDDD(R.string.rddd, R.drawable.rddd, Cups.BANANA),
    rDP3(R.string.rdp3, R.drawable.rdp3, Cups.BANANA),
    rRRy(R.string.rrry, R.drawable.rrry, Cups.BANANA),
    rDKJ(R.string.rdkj, R.drawable.rdkj, Cups.BANANA),
    rWS(R.string.rws, R.drawable.rws, Cups.LEAF),
    rSL(R.string.rsl, R.drawable.rsl, Cups.LEAF),
    rMP(R.string.rmp, R.drawable.rmp, Cups.LEAF),
    rYV(R.string.ryv, R.drawable.ryv, Cups.LEAF),
    rTTC(R.string.rttc, R.drawable.rttc, Cups.LIGHTNING),
    rPPS(R.string.rpps, R.drawable.rpps, Cups.LIGHTNING),
    rGV(R.string.rgv, R.drawable.rgv, Cups.LIGHTNING),
    rRRd(R.string.rrrd, R.drawable.rrrd, Cups.LIGHTNING),
    dBP(R.string.dbp, R.drawable.dbp, Cups.CROSSING),
    dCL(R.string.dcl, R.drawable.dcl, Cups.CROSSING),
    dWW(R.string.dww, R.drawable.dww, Cups.CROSSING),
    dAC(R.string.dac, R.drawable.dac, Cups.CROSSING),
    dNBC(R.string.dnbc, R.drawable.dnbc, Cups.BELL),
    dRiR(R.string.drir, R.drawable.drir, Cups.BELL),
    dSBS(R.string.dsbs, R.drawable.dsbs, Cups.BELL),
    dBB(R.string.dbb, R.drawable.dbb, Cups.BELL),
    bPP(R.string.bpp, R.drawable.bpp, Cups.GOLDEN_DASH),
    bTC(R.string.btc, R.drawable.btc, Cups.GOLDEN_DASH),
    bCMo(R.string.bcmo, R.drawable.bcmo, Cups.GOLDEN_DASH),
    bCMa(R.string.bcma, R.drawable.bcma, Cups.GOLDEN_DASH),
    bTB(R.string.btb, R.drawable.btb, Cups.LUCKY_CAT),
    bSR(R.string.bsr, R.drawable.bsr, Cups.LUCKY_CAT),
    bSG(R.string.bsg, R.drawable.bsg, Cups.LUCKY_CAT),
    bNH(R.string.bnh, R.drawable.bnh, Cups.LUCKY_CAT),
    bNYM(R.string.bnym, R.drawable.bnym, Cups.TURNIP),
    bMC3(R.string.bmc3, R.drawable.bmc3, Cups.TURNIP),
    bKD(R.string.bkd, R.drawable.bkd, Cups.TURNIP),
    bWP(R.string.bwp, R.drawable.bwp, Cups.TURNIP),
    bSS(R.string.bss, R.drawable.bss, Cups.PROPELLER),
    bSL(R.string.bsl, R.drawable.bsl, Cups.PROPELLER),
    bMG(R.string.bmg, R.drawable.bmg, Cups.PROPELLER),
    bSHS(R.string.bshs, R.drawable.bshs, Cups.PROPELLER),
    bLL(R.string.bll, R.drawable.bll, Cups.ROCK),
    bBL(R.string.bbl, R.drawable.bbl, Cups.ROCK),
    bRRM(R.string.brrm, R.drawable.brrm, Cups.ROCK),
    bMT(R.string.bmt, R.drawable.bmt, Cups.ROCK),
    bBB(R.string.bbb, R.drawable.bbb, Cups.MOON),
    bPG(R.string.bpg, R.drawable.bpg, Cups.MOON),
    bMM(R.string.bmm, R.drawable.bmm, Cups.MOON),
    bRR(R.string.brr, R.drawable.brr, Cups.MOON),
    bAD(R.string.bad, R.drawable.bad, Cups.BOOMERANG),
    bRP(R.string.brp, R.drawable.brp, Cups.BOOMERANG),
    bDKS(R.string.bdksc, R.drawable.bdksc, Cups.BOOMERANG),
    bYI(R.string.byi, R.drawable.byi, Cups.BOOMERANG),
    bBR(R.string.bbr, R.drawable.bbr, Cups.FRUIT),
    bMC(R.string.bmc, R.drawable.bmc, Cups.FRUIT),
    bWS(R.string.bws, R.drawable.bws, Cups.FRUIT),
    bSiS(R.string.bsis, R.drawable.bsis, Cups.FRUIT)

}
