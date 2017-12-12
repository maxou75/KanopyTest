package fr.kanopytest.maximedonnet.kanopytest;

/**
 * This class is used to instantiate commits objects that are retrieved from GitHub API with their basic information.
 * The Commits object are displayed by the CommitAdapter.
 */
public class Commit {
    private String sha;
    private String committer;
    private String date;
    private String title;
    private String url;

    public Commit(String sha, String committer, String title, String date, String url) {
        this.sha = sha;
        this.committer = committer;
        this.date = date;
        this.title = title;
        this.url = url;
    }

    public String getSha() {
        return this.sha;
    }

    public String getUrl() {
        return this.url;
    }

    public String getCommitter() {
        return this.committer;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDate() {
        return this.date;
    }
}
