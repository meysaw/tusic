package com.tusic;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

public class App {

    public static void main(String[] args) throws Exception {
        DBHelper db = new DBHelper();
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
        BasicWindow window = new BasicWindow("TUSIC SYSTEM");
        window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

        Panel mainPanel = new Panel(new BorderLayout());

        Panel headerContainer = new Panel(new LinearLayout(Direction.VERTICAL));

        Label titleLabel = new Label("TUSIC TERMINAL PLAYER")
                .addStyle(SGR.BOLD)
                .setForegroundColor(TextColor.ANSI.CYAN);
        headerContainer.addComponent(titleLabel, LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        headerContainer.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel tabContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Label musicTab = new Label(" 8: LIBRARY ").setBackgroundColor(TextColor.ANSI.BLUE).addStyle(SGR.BOLD);
        Label downloadsTab = new Label(" 9: DOWNLOADS ").setBackgroundColor(TextColor.ANSI.BLACK);

        tabContainer.addComponent(musicTab);
        tabContainer.addComponent(new EmptySpace(new TerminalSize(4, 1)));
        tabContainer.addComponent(downloadsTab);

        headerContainer.addComponent(tabContainer, LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        mainPanel.addComponent(headerContainer.withBorder(Borders.singleLine()), BorderLayout.Location.TOP);

        Player player = new Player();

        Panel musicPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        musicPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        musicPanel.addComponent(new Label("AVAILABLE TRACKS").addStyle(SGR.BOLD).setForegroundColor(TextColor.ANSI.WHITE));

        ActionListBox songList = new ActionListBox(new TerminalSize(60, 10));
        ArrayList<String> songs = DBHelper.listSongs();

        Label nowPlayingLabel = new Label("Not playing ....").setForegroundColor(TextColor.ANSI.MAGENTA);

        for (String song : songs) {
            String fileName = song.substring(song.lastIndexOf('/') + 1);
            songList.addItem(fileName.replace(".mp3", " "), () -> {
                player.play(song);
                nowPlayingLabel.setText("Now playing: " + fileName.replace(".mp3", " "));
            });
        }
        musicPanel.addComponent(songList);

        Panel downloadsPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        downloadsPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        downloadsPanel.addComponent(new Label("EXTERNAL DOWNLOADER").addStyle(SGR.BOLD).setForegroundColor(TextColor.ANSI.WHITE));

        downloadsPanel.addComponent(new Label("Source URL:"));
        TextBox linkInput = new TextBox(new TerminalSize(55, 1));
        downloadsPanel.addComponent(linkInput);

        TextBox logBox = new TextBox(new TerminalSize(60, 8), TextBox.Style.MULTI_LINE).setReadOnly(true);
        downloadsPanel.addComponent(new Label("PROCESS LOG:"));
        downloadsPanel.addComponent(logBox);

        Component musicView = musicPanel.withBorder(Borders.doubleLine());
        Component downloadView = downloadsPanel.withBorder(Borders.doubleLine());

        mainPanel.addComponent(musicView, BorderLayout.Location.CENTER);
        downloadView.setVisible(false);
        mainPanel.addComponent(downloadView, BorderLayout.Location.CENTER);

        Panel footerPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Panel nowPlayingPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        nowPlayingPanel.addComponent(nowPlayingLabel);
        footerPanel.addComponent(nowPlayingPanel.withBorder(Borders.singleLine("")));

        Panel statusRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Label statusLabel = new Label(" READY ").setBackgroundColor(TextColor.ANSI.GREEN).setForegroundColor(TextColor.ANSI.BLACK);
        statusRow.addComponent(statusLabel);
        statusRow.addComponent(new Label("  Q: Quit | 8: Library | 9: Downloads | use ->|(tab) to navigate ").setForegroundColor(TextColor.ANSI.YELLOW));

        footerPanel.addComponent(statusRow);
        mainPanel.addComponent(footerPanel, BorderLayout.Location.BOTTOM);

        Button downloadBtn = new Button("DOWNLOAD ", () -> {
            logBox.setText(" ");

            String link = linkInput.getText().trim();
            linkInput.setText(" ");
            if (link.isEmpty()) {
                logBox.addLine("  ERROR | Input URL is empty");

                return;
            }
            new Thread(() -> {
                gui.getGUIThread().invokeLater(()
                        -> logBox.addLine(" INFO | Establishing connection to source...")
                );

                String filePath = YTDLHelper.downloadAudio(link);

                gui.getGUIThread().invokeLater(() -> {
                    if (filePath != null) {
                        String name = new File(filePath).getName();
                        logBox.addLine(" SUCCESS | " + name + " has been downloaded: ");
                        db.addSong(name, filePath);
                        songList.addItem(name, () -> {
                            player.play(filePath);
                            nowPlayingLabel.setText("Active: " + name);
                        });
                    } else {
                        logBox.addLine(" | Error | Download process terminated unexpectedly");
                    }
                });
            }).start();
        });
        downloadsPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        downloadsPanel.addComponent(downloadBtn, LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onUnhandledInput(Window baseWindow, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                if (keyStroke.getCharacter() != null) {
                    switch (keyStroke.getCharacter()) {
                        case '8':
                            musicView.setVisible(true);
                            downloadView.setVisible(false);
                            musicTab.setBackgroundColor(TextColor.ANSI.BLUE);
                            downloadsTab.setBackgroundColor(TextColor.ANSI.BLACK);
                            statusLabel.setText(" LIBRARY ");
                            break;
                        case '9':
                            musicView.setVisible(false);
                            downloadView.setVisible(true);
                            linkInput.takeFocus();
                            musicTab.setBackgroundColor(TextColor.ANSI.BLACK);
                            downloadsTab.setBackgroundColor(TextColor.ANSI.BLUE);
                            statusLabel.setText(" DOWNLOADS ");
                            break;
                        case 'q':
                        case 'Q':
                            baseWindow.close();
                            db.close();
                            player.stop();
                            break;
                    }
                }
            }
        });

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
        screen.stopScreen();
    }
}
