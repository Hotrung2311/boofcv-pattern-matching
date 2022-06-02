

import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.misc.PixelMath;
import boofcv.alg.template.TemplateMatching;
import boofcv.alg.template.TemplateMatchingIntensity;
import boofcv.factory.template.FactoryTemplateMatching;
import boofcv.factory.template.TemplateScoreType;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class PatternMatching {
	
    private static List<Match> findMatches(GrayF32 image, GrayF32 template, GrayF32 mask,
                                           int expectedMatches ) {
        // create template matcher.
        TemplateMatching<GrayF32> matcher =
                FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_SQUARE_ERROR, GrayF32.class);

        // Find the points which match the template the best
        matcher.setImage(image);
        matcher.setTemplate(template, mask, expectedMatches);
        matcher.process();

        return matcher.getResults().toList();
    }

    /**
     * Computes the template match intensity image and displays the results. Brighter intensity indicates
     * a better match to the template.
     */
    private static void showMatchIntensity( GrayF32 image, GrayF32 template, GrayF32 mask ) {
        // create algorithm for computing intensity image
        TemplateMatchingIntensity<GrayF32> matchIntensity =
                FactoryTemplateMatching.createIntensity(TemplateScoreType.SUM_SQUARE_ERROR, GrayF32.class);

        // apply the template to the image
        matchIntensity.setInputImage(image);
        matchIntensity.process(template, mask);

        // get the results
        GrayF32 intensity = matchIntensity.getIntensity();

        // White will indicate a good match and black a bad match, or the reverse
        // depending on the cost function used.
        float min = ImageStatistics.min(intensity);
        float max = ImageStatistics.max(intensity);
        float range = max - min;
        PixelMath.plus(intensity, -min, intensity);
        PixelMath.divide(intensity, range, intensity);
        PixelMath.multiply(intensity, 255.0f, intensity);

        BufferedImage output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
        VisualizeImageData.grayMagnitude(intensity, output, -1);
        ShowImages.showWindow(output, "Match Intensity", true);
    }

    public static void main( String[] args ) {
        // Load image and templates
        String directory = UtilIO.pathExample("/Users/hoductrung/eclipse-workspace/fiducial-image-example/img");
        String templateDir = UtilIO.pathExample("/Users/hoductrung/eclipse-workspace/fiducial-image-example/patterns");

        // Original image
        GrayF32 image = UtilImageIO.loadImage(directory, "map.png", GrayF32.class);

        // Templates fiducial to
        GrayF32 rice = UtilImageIO.loadImage(templateDir, "Rice.png", GrayF32.class);
        GrayF32 vege = UtilImageIO.loadImage(templateDir, "Vegetables.png", GrayF32.class);
        GrayF32 aqua = UtilImageIO.loadImage(templateDir, "Aquaculture.png", GrayF32.class);
        GrayF32 industrial = UtilImageIO.loadImage(templateDir, "Industrial.png", GrayF32.class);

        // create output image to show results
        var output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
        ConvertBufferedImage.convertTo(image, output);
        Graphics2D g1 = output.createGraphics();
        Graphics2D g2 = output.createGraphics();
        Graphics2D g3 = output.createGraphics();
        Graphics2D g4 = output.createGraphics();

        // Find and mark
        // Vegetable
        g1.setColor(Color.BLUE);
        g1.setStroke(new BasicStroke(3));
        markImage(g1, image, vege, null, 3, "Vegetables");

        // Rice
        g2.setColor(Color.ORANGE);
        g2.setStroke(new BasicStroke(3));
        markImage(g2, image, rice, null, 3, "Rice");

        // Aquaculture
        g3.setColor(Color.GREEN);
        g3.setStroke(new BasicStroke(3));
        markImage(g3, image, aqua, null, 3, "Aquaculture");

        // Industrial
        g4.setColor(Color.MAGENTA);
        g4.setStroke(new BasicStroke(3));
        markImage(g4, image, industrial, null, 2, "Industrial");

        ShowImages.showWindow(output, "Found Matches", true);
    }

    /**
     * Helper function will is finds matches and displays the results as colored rectangles
     */
    private static void markImage( Graphics2D g2,
                                        GrayF32 image, GrayF32 template, GrayF32 mask,
                                        int expectedMatches, String type ) {
        List<Match> found = findMatches(image, template, mask, expectedMatches);

        int r = 2;
        int w = template.width + 2*r;
        int h = template.height + 2*r;

        for (Match m : found) {
            System.out.println(type + ": x = " + m.x + ", y =" + m.y);

            // the return point is the template's top left corner
            int x0 = m.x - r;
            int y0 = m.y - r;
            int x1 = x0 + w;
            int y1 = y0 + h;

            g2.drawLine(x0, y0, x1, y0);
            g2.drawLine(x1, y0, x1, y1);
            g2.drawLine(x1, y1, x0, y1);
            g2.drawLine(x0, y1, x0, y0);
            g2.drawString(type, x0, y0-10);
        }
    }
}
