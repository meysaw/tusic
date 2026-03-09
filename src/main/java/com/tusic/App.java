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
        Player player = new Player();

        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
        BasicWindow window = new BasicWindow("TUSIC SYSTEM");
        window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

        // ── Main layout ─────────────────────────────────────────────
        Panel mainPanel = new Panel(new BorderLayout());

        // ── Header ──────────────────────────────────────────────────
        Panel headerContainer = new Panel(new LinearLayout(Direction.VERTICAL));

        Label titleLabel = new Label("TUSIC TERMINAL PLAYER")
                .addStyle(SGR.BOLD)
                .setForegroundColor(TextColor.ANSI.CYAN);
        headerContainer.addComponent(titleLabel,
                LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        headerContainer.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel tabContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Label musicTab = new Label(" 8: LIBRARY ")
                .setBackgroundColor(TextColor.ANSI.BLUE).addStyle(SGR.BOLD);
        Label downloadsTab = new Label(" 9: DOWNLOADS ")
                .setBackgroundColor(TextColor.ANSI.BLACK);

        tabContainer.addComponent(musicTab);
        tabContainer.addComponent(new EmptySpace(new TerminalSize(4, 1)));
        tabContainer.addComponent(downloadsTab);

        headerContainer.addComponent(tabContainer,
                LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        mainPanel.addComponent(headerContainer.withBorder(Borders.singleLine()),
                BorderLayout.Location.TOP);

        // ── Now-playing label (shared between views) ────────────────
        Label nowPlayingLabel = new Label("Not playing ....")
                .setForegroundColor(TextColor.ANSI.MAGENTA);

        // ── Library panel ───────────────────────────────────────────
        Panel musicPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        musicPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        musicPanel.addComponent(new Label("AVAILABLE TRACKS")
                .addStyle(SGR.BOLD).setForegroundColor(TextColor.ANSI.WHITE));

        ActionListBox songList = new ActionListBox(new TerminalSize(60, 10));
        loadSongList(db, songList, player, nowPlayingLabel);
        musicPanel.addComponent(songList);

        // ── Downloads panel ─────────────────────────────────────────
        Panel downloadsPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        downloadsPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        downloadsPanel.addComponent(new Label("EXTERNAL DOWNLOADER")
                .addStyle(SGR.BOLD).setForegroundColor(TextColor.ANSI.WHITE));

        downloadsPanel.addComponent(new Label("Source URL:"));
        TextBox linkInput = new TextBox(new TerminalSize(55, 1));
        downloadsPanel.addComponent(linkInput);

        TextBox logBox = new TextBox(new TerminalSize(60, 8), TextBox.Style.MULTI_LINE)
                .setReadOnly(true);
        downloadsPanel.addComponent(new Label("PROCESS LOG:"));
        downloadsPanel.addComponent(logBox);

        Button downloadBtn = new Button("DOWNLOAD", () -> {
            logBox.setText(" ");

            String link = linkInput.getText().trim();
            linkInput.setText("");
            if (link.isEmpty()) {
                logBox.addLine("  ERROR | Input URL is empty");
                return;
            }

            new Thread(() -> {
                guiLog(gui, logBox, "INFO | Starting download...");

                String filePath = YTDLHelper.downloadAudio(link, message ->
                        guiLog(gui, logBox, message)
                );

                gui.getGUIThread().invokeLater(() -> {
                    if (filePath != null) {
                        String name = new File(filePath).getName();
                        logBox.addLine("SUCCESS | " + name + " added to library");
                        db.addSong(name, filePath);
                        songList.addItem(name.replace(".mp3", ""), () -> {
                            player.play(filePath);
                            nowPlayingLabel.setText("Now playing: " + name.replace(".mp3", ""));
                        });
                    } else {
                        logBox.addLine("ERROR | Download failed. Check the URL and try again.");
                    }
                });
            }, "download-thread").start();
        });

        downloadsPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        downloadsPanel.addComponent(downloadBtn,
                LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

        // ── View management ─────────────────────────────────────────
        Component musicView = musicPanel.withBorder(Borders.doubleLine());
        Component downloadView = downloadsPanel.withBorder(Borders.doubleLine());

        mainPanel.addComponent(musicView, BorderLayout.Location.CENTER);
        downloadView.setVisible(false);
        mainPanel.addComponent(downloadView, BorderLayout.Location.CENTER);

        // ── Footer ──────────────────────────────────────────────────
        Panel footerPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Panel nowPlayingPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        nowPlayingPanel.addComponent(nowPlayingLabel);
        footerPanel.addComponent(nowPlayingPanel.withBorder(Borders.singleLine("")));

        Panel statusRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Label statusLabel = new Label(" READY ")
                .setBackgroundColor(TextColor.ANSI.GREEN)
                .setForegroundColor(TextColor.ANSI.BLACK);
        statusRow.addComponent(statusLabel);
        statusRow.addComponent(new Label(
                "  Q: Quit | 8: Library | 9: Downloads | use ->|(tab) to navigate ")
                .setForegroundColor(TextColor.ANSI.YELLOW));

        footerPanel.addComponent(statusRow);
        mainPanel.addComponent(footerPanel, BorderLayout.Location.BOTTOM);

        // ── Keyboard shortcuts ──────────────────────────────────────
        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onUnhandledInput(Window baseWindow, KeyStroke keyStroke,
                    AtomicBoolean hasBeenHandled) {
                if (keyStroke.getCharacter() == null) return;

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
                        player.stop();
                        db.close();
                        baseWindow.close();
                        break;
                }
            }
        });

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
        screen.stopScreen();
    }

    /**
     * Load existing songs from the database into the song list widget.
     */
    private static void loadSongList(DBHelper db, ActionListBox songList,
            Player player, Label nowPlayingLabel) {
        ArrayList<String> songs = db.listSongs();
        for (String songPath : songs) {
            String fileName = songPath.substring(songPath.lastIndexOf('/') + 1);
            String displayName = fileName.replace(".mp3", "");
            songList.addItem(displayName, () -> {
                player.play(songPath);
                nowPlayingLabel.setText("Now playing: " + displayName);
            });
        }
    }

    /**
     * Thread-safe helper to append a log line to the log TextBox.
     */
    private static void guiLog(MultiWindowTextGUI gui, TextBox logBox, String message) {
        gui.getGUIThread().invokeLater(() -> logBox.addLine(" " + message));
    }
}
