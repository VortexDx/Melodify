package com.vortico.melodify;

public class DatabaseBlueprint {

    String songName;
    String songUri;

    public DatabaseBlueprint(String songName, String songUri) {
        this.songName = songName;
        this.songUri = songUri;
    }

    public String getSongName() {
        return songName;
    }

    public String getSongUri() {
        return songUri;
    }
}
