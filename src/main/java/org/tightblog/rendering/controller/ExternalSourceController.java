package org.tightblog.rendering.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.Template;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = ExternalSourceController.PATH)
// Validate constraint annotations on method parameters
@Validated
public class ExternalSourceController {

    private static Logger log = LoggerFactory.getLogger(ExternalSourceController.class);

    private ThymeleafRenderer thymeleafRenderer;

    public static final String PATH = "/tb-ui/rendering/external";

    private static final MediaType APP_JAVASCRIPT = new MediaType("application", "javascript");

    public ExternalSourceController(@Qualifier("atomRenderer") ThymeleafRenderer thymeleafRenderer) {
        this.thymeleafRenderer = thymeleafRenderer;
    }

    @GetMapping(path = "/github/**")
    ResponseEntity<String> getGithubSource(HttpServletRequest request,
                                           @RequestParam(value = "start", required = false) Integer startLine,
                                           @RequestParam(value = "end", required = false) Integer endLine,
                                           @RequestParam(value = "height", required = false) Integer height,
                                           @RequestParam(value = "linenums", required = false, defaultValue = "true")
                                                   boolean showLinenums
    ) throws IOException {

        // sample servlet path:
        // /tb-ui/rendering/external/github/gmazza/blog-samples/raw/master/jaxws_handler_tutorial/...
        // client/src/main/java/client/ClientHandlers.java
        String path = request.getServletPath();

        // resultant desired GitHub URL to retrieve from (if /blob/ instead of /raw/, need to switch to latter):
        // https://www.github.com/gmazza/blog-samples/raw/master/jaxws_handler_tutorial/...
        // client/src/main/java/client/ClientHandlers.java

        // get portion starting from "gmazza"
        int projectPosition = StringUtils.ordinalIndexOf(path, "/", 5);

        if (projectPosition == -1) {
            return ResponseEntity.notFound().build();
        }

        // path2 = mazza/blog-samples/raw/master/...
        String path2 =  path.substring(projectPosition + 1);

        // change "/blob/" to "/raw/" if necessary
        int rawPositionBefore = StringUtils.ordinalIndexOf(path2, "/", 2);
        int rawPositionAfter = StringUtils.ordinalIndexOf(path2, "/", 3);

        if (rawPositionBefore == -1 || rawPositionAfter == -1) {
            return ResponseEntity.notFound().build();
        }

        String githubURL = String.format("https://www.github.com/%s/raw/%s",
            path2.substring(0, rawPositionBefore), path2.substring(rawPositionAfter + 1));

        RestTemplate rt = new RestTemplate();
        String sourceCode = rt.getForObject(githubURL, String.class, new HashMap<>());

        if (sourceCode == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> model = new HashMap<>();
        if (height != null) {
            model.put("height", height);
            model.put("startLine", 1);
            model.put("showLinenums", showLinenums);
        }

        // if requested show specific lines only (\n is a line indicator), first line is 1 or 0, second line is 2.
        if (endLine != null && endLine >= 0 && (startLine == null || endLine >= startLine)) {
            int end = StringUtils.ordinalIndexOf(sourceCode, "\n", endLine);
            if (end > -1) {
                sourceCode = sourceCode.substring(0, end);
            }
        }

        if (startLine != null && startLine > 1 && (endLine == null || startLine <= endLine)) {
            int beginning = StringUtils.ordinalIndexOf(sourceCode, "\n", startLine - 1);
            if (beginning > -1) {
                sourceCode = sourceCode.substring(beginning);
                model.put("startLine", startLine);
            }
        }

        model.put("githubSource", sourceCode);

        Template template = new SharedTemplate("github-source", Template.Role.JAVASCRIPT);
        CachedContent rendererOutput = thymeleafRenderer.render(template, model);

        String content = new String(rendererOutput.getContent(), StandardCharsets.UTF_8);

        // place new lines in output html, suitable for <pre> tags.
        content = content.replace("\n", "\\n");

        // important: document.write(...) must be on one line, no matter how long
        String response = String.format("document.write('%s');", content);

        return ResponseEntity.ok()
                .contentType(APP_JAVASCRIPT)
                .contentLength(response.length())
                .cacheControl(CacheControl.noCache())
                .body(response);

    }
}
