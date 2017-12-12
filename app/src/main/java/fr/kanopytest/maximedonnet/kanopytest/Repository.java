package fr.kanopytest.maximedonnet.kanopytest;

/**
 * This class is used to instantiate repositories objects that are retrieved from GitHub API with their basic information.
 * The repositories object are displayed by the RepositoryAdapter.
 */
public class Repository {
    private String id;
    private String url;
    private String full_name;

    Repository(String id, String url, String full_name) {
        this.id = id;
        this.url = url;
        this.full_name = full_name;
    }

    public String getFullName() {
        return this.full_name;
    }

    public String getId() {
        return this.id;
    }

    public String getCommitsUrl() {
        return this.url + "/commits";
    }
}
