package main.java.com.ffc.bot.responseTextModule;

public class ResponseTextBuilder {

    public StringBuilder sb = new StringBuilder();

//    public ResponseTextBuilder() {
//        sb = new StringBuilder();
//    }

    public ResponseTextBuilder startFormat(TextFormat format) {
        sb.append("<").append(getFormat(format)).append(">");
        return this;
    }

    public ResponseTextBuilder endFormat(TextFormat format) {
        sb.append("</").append(getFormat(format)).append(">");
        return this;
    }

    public ResponseTextBuilder addText(String text) {
        sb.append(" ");
        sb.append(text);
        return this;
    }

    public ResponseTextBuilder addText(String text, TextFormat[] formats) {
        sb.append(" ");
        for (TextFormat format: formats) {
            sb.append("<").append(getFormat(format)).append(">");
        }
        sb.append(text);
        for (int i = formats.length-1; i >=0 ; i--) {
            sb.append("</").append(getFormat(formats[i])).append(">");
        }
        return this;
    }

    public ResponseTextBuilder addText(String text, TextFormat format) {
        sb.append(" ");
        sb.append("<").append(getFormat(format)).append(">")
                .append(text)
                .append("</").append(getFormat(format)).append(">");
        return this;
    }

    public ResponseTextBuilder addTextLine() {
        sb.append("\n");
        return this;
    }

    public ResponseTextBuilder addTextLine(String text) {
        sb.append("\n");
        sb.append(text);
        return this;
    }

    public ResponseTextBuilder addTextLine(String text, TextFormat format) {
        sb.append("\n");
        sb.append("<").append(getFormat(format)).append(">")
                .append(text)
                .append("</").append(getFormat(format)).append(">");
        return this;
    }

    public ResponseTextBuilder addTextLine(String text, TextFormat[] formats) {
        sb.append("\n");
        for (TextFormat format: formats) {
            sb.append("<").append(getFormat(format)).append(">");
        }
        sb.append(text);
        for (int i = formats.length-1; i >=0 ; i--) {
            sb.append("</").append(getFormat(formats[i])).append(">");
        }
        return this;
    }

    public String get() {
        String result = sb.toString();
        sb = new StringBuilder();
        return result;
    }

    private String getFormat(TextFormat format) {
        switch (format) {
            case Bold: return "b";
            case Italic: return "i";
            case Underlined: return "u";
            case Strike: return "strike";
            case Monocular: return "code";
        }
        return null;
    }
}
