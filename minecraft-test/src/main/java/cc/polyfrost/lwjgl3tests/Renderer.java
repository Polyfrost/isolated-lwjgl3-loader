package cc.polyfrost.lwjgl3tests;

import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.libs.universal.UScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NanoVGGL3;
//#if MODERN && FORGE
//$$ import com.mojang.blaze3d.matrix.MatrixStack;
//#elseif MODERN && FABRIC
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

public class Renderer {
    static {
        Loader.load();
    }

    private static long ctx = -1;

    public static void render(
            //#if MODERN
            //$$ MatrixStack matrixStack
            //#endif
    ) {
        GuiScreen.drawRect(
                //#if MODERN
                //$$ matrixStack,
                //#endif
                10, 10, 10  + 100, 10 + 100, 0x99FF0000);

        if (ctx == -1) {
            ctx = NanoVGGL2.nvgCreate(NanoVGGL2.NVG_ANTIALIAS | NanoVGGL2.NVG_STENCIL_STROKES);
        } else {
            NanoVG.nvgBeginFrame(ctx, UResolution.getWindowWidth(), UResolution.getWindowHeight(), 1);
            NanoVG.nvgBeginPath(ctx);
            NanoVG.nvgRoundedRect(ctx, 50, 50, 100, 100, 12);
            NVGColor color = NVGColor.create();
            NanoVG.nvgRGBAf(0, 0, 1, 0.5f, color);
            NanoVG.nvgFillColor(ctx, color);
            NanoVG.nvgFill(ctx);
            NanoVG.nvgClosePath(ctx);
            NanoVG.nvgEndFrame(ctx);

            NanoVG.nvgBeginFrame(ctx, UResolution.getWindowWidth(), UResolution.getWindowHeight(), (float) UResolution.getScaleFactor());
            NanoVG.nvgBeginPath(ctx);
            NanoVG.nvgRoundedRect(ctx, 200, 200, 100, 100, 12);
            NVGColor color2 = NVGColor.create();
            NanoVG.nvgRGBAf(0, 1, 0, 0.5f, color2);
            NanoVG.nvgFillColor(ctx, color2);
            NanoVG.nvgFill(ctx);
            NanoVG.nvgClosePath(ctx);
            NanoVG.nvgEndFrame(ctx);
        }
    }
}
