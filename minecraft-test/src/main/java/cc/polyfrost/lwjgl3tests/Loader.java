package cc.polyfrost.lwjgl3tests;

import cc.polyfrost.lwjgl.bootstrap.Lwjgl3Bootstrap;

import java.io.IOException;

public class Loader {
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        loaded = true;

        System.out.println("Loading lwjgl3-bootstrap.");
        try {
            Lwjgl3Bootstrap.INSTANCE.initialize(getMinecraftVersion());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static int getMinecraftVersion() {
        //#if MC==11902
        //$$ return 11902;
        //#elseif MC==11901
        //$$ return 11901;
        //#elseif MC==11900
        //$$ return 11900;
        //#elseif MC==11802
        //$$ return 11802;
        //#elseif MC==11801
        //$$ return 11801;
        //#elseif MC==11800
        //$$ return 11800;
        //#elseif MC==11702
        //$$ return 11702;
        //#elseif MC==11701
        //$$ return 11701;
        //#elseif MC==11700
        //$$ return 11700;
        //#elseif MC==11605
        //$$ return 11605;
        //#elseif MC==11604
        //$$ return 11604;
        //#elseif MC==11603
        //$$ return 11603;
        //#elseif MC==11602
        //$$ return 11602;
        //#elseif MC==11601
        //$$ return 11601;
        //#elseif MC==11600
        //$$ return 11600;
        //#elseif MC==11502
        //$$ return 11502;
        //#elseif MC==11501
        //$$ return 11501;
        //#elseif MC==11500
        //$$ return 11500;
        //#elseif MC==11404
        //$$ return 11404;
        //#elseif MC==11403
        //$$ return 11403;
        //#elseif MC==11402
        //$$ return 11402;
        //#elseif MC==11401
        //$$ return 11401;
        //#elseif MC==11400
        //$$ return 11400;
        //#elseif MC==11302
        //$$ return 11302;
        //#elseif MC==11301
        //$$ return 11301;
        //#elseif MC==11300
        //$$ return 11300;
        //#elseif MC==11202
        //$$ return 11202;
        //#elseif MC==11201
        //$$ return 11201;
        //#elseif MC==11200
        //$$ return 11200;
        //#elseif MC==11102
        //$$ return 11102;
        //#elseif MC==11101
        //$$ return 11101;
        //#elseif MC==11100
        //$$ return 11100;
        //#elseif MC==11002
        //$$ return 11002;
        //#elseif MC==11001
        //$$ return 11001;
        //#elseif MC==11000
        //$$ return 11000;
        //#elseif MC==10904
        //$$ return 10904;
        //#elseif MC==10903
        //$$ return 10903;
        //#elseif MC==10902
        //$$ return 10902;
        //#elseif MC==10901
        //$$ return 10901;
        //#elseif MC==10900
        //$$ return 10900;
        //#elseif MC==10809
        return 10809;
        //#elseif MC==10808
        //$$ return 10808;
        //#elseif MC==10807
        //$$ return 10807;
        //#elseif MC==10806
        //$$ return 10806;
        //#elseif MC==10805
        //$$ return 10805;
        //#elseif MC==10804
        //$$ return 10804;
        //#elseif MC==10803
        //$$ return 10803;
        //#elseif MC==10802
        //$$ return 10802;
        //#elseif MC==10801
        //$$ return 10801;
        //#elseif MC==10800
        //$$ return 10800;
        //#elseif MC==10710
        //$$ return 10710;
        //#endif
    }
}
