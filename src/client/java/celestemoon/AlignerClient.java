package celestemoon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AlignerClient implements ClientModInitializer {
	private boolean between(double l, double var, double r) {
		return l < var && var <= r; // interval: (l, r]
	}

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		KeyBinding keyAlign = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.aligner.align",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_0,
				"category.aligner.key.align"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyAlign.wasPressed()) {
                if (client.player != null) {
					double x = (double)client.player.getBlockX() + 0.5;
					double y = client.player.getY();
					double z = (double)client.player.getBlockZ() + 0.5;

					double yaw = client.player.getYaw(), pitch = client.player.getPitch();
					/*
					minecraft stores yaw value using a float variable which can exceed the limit of +180 and -180
					(which you usually see on F3 screen), so we have to convert it.
					*/
					yaw %= 360; if (yaw < -180.0) yaw += 360; else if (yaw > 180.0) yaw -= 360;

					double fixedYaw = -180.0, fixedPitch = 0.0;
					if (between(-22.5, yaw, 22.5)) fixedYaw = 0.0;
					else if (between(22.5, yaw, 67.5)) fixedYaw = 45.0;
					else if (between(67.5, yaw, 112.5)) fixedYaw = 90.0;
					else if (between(112.5, yaw, 157.5)) fixedYaw = 135.0;
					else if (between(-67.5, yaw, -22.5)) fixedYaw = -45.0;
					else if (between(-112.5, yaw, -67.5)) fixedYaw = -90.0;
					else if (between(-157.5, yaw, -112.5)) fixedYaw = -135.0;
					// -180.0 to avoid pitch == -90 problem causes by interval (l, r]; 180.0 to be symmetrical
					if (between(-180.0, pitch, -67.5)) fixedPitch = -90.0;
					else if (between(-67.5, pitch, -22.5)) fixedPitch = -45.0;
					else if (between(22.5, pitch, 67.5)) fixedPitch = 45.0;
					else if (between(67.5, pitch, 180.0)) fixedPitch = 90.0;

					String coordinatesRaw = x + " " + y + " " + z + " " + fixedYaw + " " + fixedPitch;
					String coordinates = "[" + x + ", " + y + ", " + z + "]" + " (" + fixedYaw + ", " + fixedPitch + ")";
					// for the sake of anticheat plugins, let's use tp commands instead of player.set___() in multiplayer
					if (client.isInSingleplayer()) {
						client.player.setPos(x, y, z);
						client.player.setYaw((float)fixedYaw);
						client.player.setPitch((float)fixedPitch);
					}
					else {
						client.player.networkHandler.sendChatCommand("tp @s " + coordinatesRaw);
					}
					String overlayText = Text.translatable("msg.aligner.align").getString() + coordinates;
					client.player.sendMessage(Text.of(overlayText), true);
				}
            }
		});
	}
}