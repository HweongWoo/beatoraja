package bms.player.beatoraja.select.bar;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.song.SongData;

/**
 * SQLで問い合わせた楽曲を表示するためのバー
 *
 * @author exch
 */
public class CommandBar extends DirectoryBar {

    private String title;
    private String sql;

    public CommandBar(MusicSelector selector, String title, String sql) {
    	super(selector);
        this.title = title;
        this.sql = sql;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Bar[] getChildren() {
    	final MainController main = selector.main;
        return SongBar.toSongBarArray(main.getSongDatabase().getSongDatas(sql,"player/" + main.getConfig().getPlayername() + "/score.db"
        		,"player/" + main.getConfig().getPlayername() + "/scorelog.db",main.getInfoDatabase() != null ? "songinfo.db" : null));
    }

    public void updateFolderStatus() {
    	final MainController main = selector.main;
        updateFolderStatus(main.getSongDatabase().getSongDatas(sql,"player/" + main.getConfig().getPlayername() + "/score.db"
        		,"player/" + main.getConfig().getPlayername() + "/scorelog.db",main.getInfoDatabase() != null ? "songinfo.db" : null));
    }
}
