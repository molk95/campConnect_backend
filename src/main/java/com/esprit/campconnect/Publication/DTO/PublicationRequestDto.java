package com.esprit.campconnect.Publication.DTO;

public class PublicationRequestDto {

    private Long forumId;
    private ForumRef forum;
    private String titre;
    private String contenu;

    public Long getForumId() {
        return forumId;
    }

    public void setForumId(Long forumId) {
        this.forumId = forumId;
    }

    public ForumRef getForum() {
        return forum;
    }

    public void setForum(ForumRef forum) {
        this.forum = forum;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Long resolveForumId() {
        if (forumId != null) {
            return forumId;
        }
        return forum != null ? forum.getId() : null;
    }

    public static class ForumRef {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
