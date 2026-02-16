### Parallel File Downloader

A high-performance Kotlin utility for parallel file downloads using Coroutines and Ktor.

#### Project Structure
- `src/main/kotlin/`
    - `Main.kt`: Application entry point.
    - `config/Config.kt`: Configuration for target URL, chunk size, and worker count.
    - `model/`: Data classes for `Chunk` ranges and `FileMetadata`.
    - `utils/`:
        - `DownloadManager`: Orchestrates the download process.
        - `Fetcher`: Retrieves file metadata (size, range support) via HEAD requests.
        - `ChunkCalculator`: Logic for splitting the file into optimal download ranges.
        - `Downloader`: Handles individual chunk downloads using HTTP Range headers.
        - `ChunkWriter`: Manages thread-safe, random-access writes to the destination file.
    - `exceptions/`: Custom exceptions for network, timeout, and I/O errors.

#### How It Works
1. **Metadata Discovery**: The tool sends a HEAD request to the target URL to check the file size and verify if the server supports range-based requests (`Accept-Ranges: bytes`).
2. **Segmentation**: Based on the file size and configured limits, `ChunkCalculator` divides the file into multiple byte ranges.
3. **Space Pre-allocation**: `ChunkWriter` creates the destination file and pre-allocates the full size on disk to prevent fragmentation and ensure write availability.
4. **Parallel Execution**: Each chunk is downloaded independently in its own coroutine.
5. **Random Access Writes**: Workers write their downloaded data directly to their assigned offset in the file using `FileChannel`, eliminating the need for a post-download merge step.

#### Components
- **Ktor (CIO Engine)**: Asynchronous HTTP client for non-blocking I/O.
- **Kotlin Coroutines**: Concurrency management for parallel chunk downloads.
- **Java NIO FileChannel**: Used for efficient random-access file operations.
- **Logback**: Provides detailed logging of the download progress and status.

#### How to Run
1. **Configure**: Update `src/main/kotlin/config/Config.kt` with the desired `url`.
2. **Execute**: Run the following command from the project root:
   ```bash
   ./gradlew run
   ```
3. **Test**: Run unit tests to verify component behavior:
   ```bash
   ./gradlew test
   ```
