
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class EOPLogParser {
	public static void main(String[] args) throws IOException {
		new EOPLogParser().run();
	}

	private void run() throws IOException {
		List<String> lines = new ArrayList<String>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[4096];
		try (Stream<Path> file = Files.find(
			Paths.get("C:/Users/karln/Downloads/exportedlogs"), 3, 
			(path, attr) -> path.getFileName().toString().endsWith(".gz"))) 
		{
			file.forEach((path) -> {
				try (GZIPInputStream zip = new GZIPInputStream(Files.newInputStream(path))) {
					int len;
					while ((len = zip.read(bytes)) != -1) {
						baos.write(bytes, 0, len);
					}
					lines.addAll(Arrays.asList(baos.toString("UTF-8").split("\\n")));
					baos.reset();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			file.close();
		}
		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get("parsedlog.txt"), StandardCharsets.UTF_8))) {
			lines
				.stream()
				.filter(l -> (l.contains("GetQuote") || l.contains("Search")))
				.map(l -> l.split(" \\[main|EncyclophiaOfPhilosophySpeechlet - "))
				.forEach(e -> pw.println(
					String.format("%1$tb-%1$td-%1$tY  %2$s", 
						java.time.ZonedDateTime.parse(e[0]), 
						e[2].replace("Intent = GetQuote", "Random Entry")
					)
				)
			);
			pw.close();
		}

	}
}
