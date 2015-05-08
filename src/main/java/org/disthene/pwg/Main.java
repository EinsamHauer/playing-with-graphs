package org.disthene.pwg;

import org.disthene.pwg.graph.Graph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args ) throws IOException {
        System.out.println( "Hello World!" );

        Graph graph = new Graph(1563, 822);
        OutputStream os = new FileOutputStream("saved.png");
        graph.render(os);
        os.flush();
        os.close();
    }
}
