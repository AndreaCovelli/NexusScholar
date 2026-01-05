package it.unipi.nexusscholar.dto.neo4j;

import java.util.List;

public class LeidenDTO {

    private int communityId;
    private List<String> authors;

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
}
