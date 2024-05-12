import static java.lang.StringTemplate.STR;

record Markdown(LiveView view) implements Clerk {
    public Markdown {
        // String onlinePath = "https://cdn.jsdelivr.net/npm/marked/marked.min.js";
        // String localPath = "clerks/Markdown/marked.min.js";
        // Clerk.load(view, onlinePath, localPath);
        String onlinePath = "https://cdn.jsdelivr.net/npm/markdown-it@14.1.0/dist/markdown-it.min.js";
        String localPath = "clerks/Markdown/markdown-it.min.js";
        Clerk.load(view, onlinePath, localPath);
        Clerk.script(view, STR."""
            const md = markdownit({
                html: true,
                linkify: true,
                typographer: true
            });
            """);
    }
    public Markdown write(String markdownText) {
        String ID = Clerk.generateID(10);
        Clerk.write(view, STR."""
            <div id="\{ID}">
            \{markdownText}
            </div>
            """);
        Clerk.call(view, STR."""
            var markdown = document.getElementById("\{ID}");
            markdown.innerHTML = md.render(markdown.textContent);
            """);
        return this;
    }
}
