package com.github.bea4dev.vanilla_source.api.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextBox {
    private static final int TEXT_WIDTH = 45;
    private static final int MAX_TITLE_WIDTH = 20;

    private final Player player;
    private final String textBox;
    private final String title;
    private final int speed;
    private final List<String> messages;
    private final List<Integer> messageLength;

    private int currentLine = 0;
    private int currentColumn = 0;
    private boolean await = false;

    private Runnable endCallBack = () -> {};

    private net.kyori.adventure.sound.Sound higherSound = net.kyori.adventure.sound.Sound.sound(
            Sound.BLOCK_NOTE_BLOCK_BIT,
            net.kyori.adventure.sound.Sound.Source.NEUTRAL,
            Float.MAX_VALUE,
            1.15F
    );
    private net.kyori.adventure.sound.Sound lowerSound = net.kyori.adventure.sound.Sound.sound(
            Sound.BLOCK_NOTE_BLOCK_BIT,
            net.kyori.adventure.sound.Sound.Source.NEUTRAL,
            Float.MAX_VALUE,
            1.25F
    );

    public void setHigherSound(net.kyori.adventure.sound.Sound higherSound) {
        this.higherSound = higherSound;
    }

    public void setLowerSound(net.kyori.adventure.sound.Sound lowerSound) {
        this.lowerSound = lowerSound;
    }

    private int tick = 0;

    public TextBox(Player player, String textBox, String title, int speed, String message) {
        this.player = player;
        this.textBox = textBox;
        this.title = title;
        this.speed = speed;
        this.messages = new ArrayList<>();
        this.messageLength = new ArrayList<>();

        var currentWidth = 0;
        var builder = new StringBuilder();
        for (char c : message.replace("/n", "\n").toCharArray()) {
            var charLength = getLength(c);
            var nextLength = currentWidth + charLength;
            if (nextLength == TEXT_WIDTH || c == '\n') {
                if (c != '\n') {
                    builder.append(c);
                }
                builder.append(" ".repeat(TEXT_WIDTH - nextLength));
                messages.add(builder.toString());

                currentWidth = 0;
                builder = new StringBuilder();
                continue;
            } else if (nextLength > TEXT_WIDTH) {
                builder.append(' ');
                messages.add(builder.toString());

                currentWidth = charLength;
                builder = new StringBuilder();
                builder.append(c);
                continue;
            }

            builder.append(c);

            currentWidth = nextLength;
        }

        if (currentWidth > 0) {
            messages.add(builder.toString());
        }

        for (var text : messages) {
            var last = 0;
            var currentLength = 0;
            for (char c : text.toCharArray()) {
                var charLength = getLength(c);
                var nextLength = currentLength + charLength;
                if (c != ' ' && c != '　') {
                    last = nextLength;
                }
                currentLength = nextLength;
            }

            messageLength.add(last);
        }
    }

    public synchronized void show() {
        TextBoxManager.registerTextBox(player, this);
    }

    public synchronized void next() {
        if (await) {
            await = false;
            currentLine++;
            currentColumn = 0;
        } else {
            currentColumn = TEXT_WIDTH - 1;
        }
    }

    synchronized void tick() {
        if (!await) {
            currentColumn += speed;
            if (currentColumn >= TEXT_WIDTH) {
                currentColumn = 0;
                currentLine++;

                if (currentLine > 1) {
                    await = true;
                    currentLine--;
                    currentColumn = TEXT_WIDTH;
                }
            }
        }

        if (currentLine >= messages.size()) {
            player.sendActionBar(Component.empty());
            TextBoxManager.unregisterTextBox(player);
            endCallBack.run();
            return;
        }

        var currentMessage = messages.get(currentLine);
        var currentMessageLast = messageLength.get(currentLine);

        if (currentColumn >= currentMessageLast) {
            currentColumn = TEXT_WIDTH;
        }

        var builder = new StringBuilder();
        var currentWidth = 0;
        for (char c : currentMessage.toCharArray()) {
            var charLength = getLength(c);
            if (currentWidth < currentColumn) {
                builder.append(c);
            } else {
                builder.append(" ".repeat(charLength));
            }
            currentWidth += charLength;
        }

        while (currentWidth < TEXT_WIDTH) {
            builder.append(' ');
            currentWidth++;
        }

        if (currentLine == 0) {
            send(builder.toString(), " ".repeat(TEXT_WIDTH));
        } else {
            send(messages.get(currentLine - 1), builder.toString());
        }

        if (!await && tick % 2 == 0) {
            if (new Random().nextInt(2) % 2 == 0) {
                player.playSound(higherSound);
            } else {
                player.playSound(lowerSound);
            }
        }

        tick++;
    }

    private void send(String first, String second) {
        var titleLength = 0;
        var builder = new StringBuilder();
        for (char c : title.toCharArray()) {
            var charLength = getLength(c);
            var nextLength = titleLength + charLength;

            if (nextLength == MAX_TITLE_WIDTH) {
                builder.append(c);
                titleLength = nextLength;
                break;
            } else if (nextLength > MAX_TITLE_WIDTH) {
                builder.append(' ');
                titleLength++;
                break;
            }

            builder.append(c);

            titleLength = nextLength;
        }

        while (titleLength < MAX_TITLE_WIDTH) {
            builder.append(' ');
            titleLength++;
        }

        var cursor = '　';
        var index = Math.abs(tick) % 20;
        if (index <= 10 && await) {
            cursor = '▼';
        }

        var component = Component.translatable("space.230").font(Key.key("space"))
                .append(Component.text(textBox).font(Key.key("default")))
                .append(Component.translatable("space.-255").font(Key.key("space")))
                .append(Component.text(builder.toString()).font(Key.key("text_0")))
                .append(Component.text(first).font(Key.key("text_1")))
                .append(Component.text(second).font(Key.key("text_2")))
                .append(Component.text(cursor).font(Key.key("text_2")));
        player.sendActionBar(component);
    }

    private static int getLength(char ch) {
        if (ch == '\n') {
            return 0;
        }

        if ((ch >= ' ' && ch <= '~') || (ch >= '｡' && ch <= 'ﾟ')) {
            return 1;
        } else {
            return 2;
        }
    }

    public void setEndCallBack(Runnable endCallBack) {
        this.endCallBack = endCallBack;
    }
}
