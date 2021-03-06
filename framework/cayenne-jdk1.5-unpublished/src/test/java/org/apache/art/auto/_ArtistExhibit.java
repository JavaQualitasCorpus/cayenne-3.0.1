package org.apache.art.auto;

/** Class _ArtistExhibit was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public abstract class _ArtistExhibit extends org.apache.cayenne.CayenneDataObject {

    public static final String TO_ARTIST_PROPERTY = "toArtist";
    public static final String TO_EXHIBIT_PROPERTY = "toExhibit";

    public static final String ARTIST_ID_PK_COLUMN = "ARTIST_ID";
    public static final String EXHIBIT_ID_PK_COLUMN = "EXHIBIT_ID";

    public void setToArtist(org.apache.art.Artist toArtist) {
        setToOneTarget("toArtist", toArtist, true);
    }

    public org.apache.art.Artist getToArtist() {
        return (org.apache.art.Artist)readProperty("toArtist");
    } 
    
    
    public void setToExhibit(org.apache.art.Exhibit toExhibit) {
        setToOneTarget("toExhibit", toExhibit, true);
    }

    public org.apache.art.Exhibit getToExhibit() {
        return (org.apache.art.Exhibit)readProperty("toExhibit");
    } 
    
    
}
