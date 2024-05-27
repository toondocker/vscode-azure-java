package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

@RestController
@RequestMapping("blob")
public class BlobController {

    @Value("azure-blob://topics/test.txt")
    private Resource blobFile;

    @GetMapping("/readBlobFile")
    public String readBlobFile() throws IOException {
        return StreamUtils.copyToString(
                this.blobFile.getInputStream(),
                Charset.defaultCharset());
    }

    @PostMapping("/writeBlobFile")
    public String writeBlobFile(@RequestBody String data) throws IOException {
        try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
            os.write(data.getBytes());
        }
        return "file was updated";
    }

    // <Snippet_SetBLobTags>
    public void setBlobTags(BlobClient blobClient) {
        // Get any existing tags for the blob if they need to be preserved
        Map<String, String> tags = blobClient.getTags();

        // Add or modify tags
        tags.put("Sealed", "false");
        tags.put("Content", "image");
        tags.put("Date", "2022-01-01");

        // setTags will replace existing tags with the map entries we pass in
        blobClient.setTags(tags);
    }

    public void uploadBlockBlobWithIndexTags(BlobContainerClient blobContainerClient, Path filePath) {
        String fileName = filePath.getFileName().toString();
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);

        Map<String, String> tags = new HashMap<String, String>();
        tags.put("Content", "image");
        tags.put("Date", "2022-01-01");

        Duration timeout = Duration.ofSeconds(10);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(filePath.toString());
        options.setTags(tags);

        try {
            // Create a new block blob, or update the content of an existing blob
            Response<BlockBlobItem> blockBlob = blobClient.uploadFromFileWithResponse(options, timeout, null);
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file: %s%n", ex.getMessage());
        }
    }
}