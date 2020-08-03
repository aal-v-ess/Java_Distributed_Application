import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;


/**
 * The SidePanel class is responsible for displaying various information
 * on the game such as the next piece, the score and current level, and controls.
 * @author Pedro Alves
 *
 */
public class SidePanel extends JPanel {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * The dimensions of each tile on the next piece preview.
	 */
	private static final int TILE_SIZE = BoardPanel.TILE_SIZE >> 1;

	/**
	 * The width of the shading on each tile on the next piece preview.
	 */
	private static final int SHADE_WIDTH = BoardPanel.SHADE_WIDTH >> 1;

	/**
	 * The number of rows and columns in the preview window. Set to
	 * 5 because we can show any piece with some sort of padding.
	 */
	private static final int TILE_COUNT = 5;

	/**
	 * The center x of the next piece preview box.
	 */
	private static final int SQUARE_CENTER_X = 130;

	/**
	 * The center y of the next piece preview box.
	 */
	private static final int SQUARE_CENTER_Y = 65;

	/**
	 * The size of the next piece preview box.
	 */
	private static final int SQUARE_SIZE = (TILE_SIZE * TILE_COUNT >> 1);

	/**
	 * The number of pixels used on a small insets.
	 */
	private static final int SMALL_INSET = 20;

	/**
	 * The number of pixels used on a large insets.
	 */
	private static final int LARGE_INSET = 40;

	/**
	 * The y coordinate of the stats category.
	 */
	private static final int STATS_INSET = 175;

	/**
	 * The y coordinate of the controls category.
	 */
	private static final int CONTROLS_INSET = 275;

	/**
	 * The number of pixels to offset between each string.
	 */
	private static final int TEXT_STRIDE = 25;

	/**
	 * The small font.
	 */
	private static final Font SMALL_FONT = new Font("Tahoma", Font.BOLD, 11);

	/**
	 * The large font.
	 */
	private static final Font LARGE_FONT = new Font("Tahoma", Font.BOLD, 13);

	/**
	 * The color to draw the text and preview box in.
	 */
	private static final Color DRAW_COLOR = new Color(0, 255, 0);	// green  0 	 255 	0


	/**
	 * The Tetris instance.
	 */
	private Tetris tetris;

	/**
	 * Creates a new SidePanel and sets it's display properties.
	 * @param tetris The Tetris instance to use.
	 */
	public SidePanel(Tetris tetris) {
		this.tetris = tetris;

		setPreferredSize(new Dimension(200, BoardPanel.PANEL_HEIGHT));
		setBackground(Color.BLACK);



	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		//Set the color for drawing.
		g.setColor(DRAW_COLOR);

		/*
		 * Stores the current y coordinate of the string.
		 */
		int offset;

		if(tetris.getStatus() == true) {
			g.setFont(LARGE_FONT);
			g.drawString("Player: " + tetris.getPlayerName(), SMALL_INSET, 120);
		}
		
		/**
		 * Draw the "Difficulty" category
		 */
		g.setFont(LARGE_FONT);
		g.drawString("Difficulty: " + tetris.getDiff(), SMALL_INSET, 150);


		/*
		 * Draw the "Stats" category.
		 */
		g.setFont(LARGE_FONT);
		g.drawString("Statistics", SMALL_INSET, offset = STATS_INSET);
		g.setFont(SMALL_FONT);
		g.drawString("Level: " + tetris.getLevel(), LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("Score: " + tetris.getScore(), LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("Lines: " + tetris.getLines(), LARGE_INSET, offset += TEXT_STRIDE);


		/*
		 * Draw the "Controls" category.
		 */
		g.setFont(LARGE_FONT);
		g.drawString("Controls", SMALL_INSET, offset = CONTROLS_INSET);
		g.setFont(SMALL_FONT);
		g.drawString("\u2190 - Move left", LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("\u2192 - Move Right", LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("\u2193 - Drop", LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("C - Rotate Anticlockwise", LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("V - Rotate Clockwise", LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("P - Pause Game", LARGE_INSET, offset += TEXT_STRIDE);
		g.drawString("ESC - Exit", LARGE_INSET, offset += TEXT_STRIDE);

		/*
		 * Draw the next piece preview box.
		 */
		g.setFont(LARGE_FONT);
		g.drawString("Next Piece:", SMALL_INSET, 70);
		g.drawRect(SQUARE_CENTER_X - SQUARE_SIZE, SQUARE_CENTER_Y - SQUARE_SIZE, SQUARE_SIZE * 2, SQUARE_SIZE * 2);


		/*
		 * Draw a preview of the next piece that will be spawned.
		 */
		TileType type = tetris.getNextPieceType();



		if(!tetris.isGameOver() && type != null) {
			/*
			 * Get the size properties of the current piece.
			 */
			int cols = type.getCols();
			int rows = type.getRows();
			int dimension = type.getDimension();

			/*
			 * Calculate the top left corner (origin) of the piece.
			 */
			int startX = (SQUARE_CENTER_X - (cols * TILE_SIZE / 2));
			int startY = (SQUARE_CENTER_Y - (rows * TILE_SIZE / 2));

			/*
			 * Get the insets for the preview. The default rotation is used for the preview(0).
			 */
			int top = type.getTopInset(0);
			int left = type.getLeftInset(0);

			/*
			 * Loop through the piece and draw it's tiles onto the preview.
			 */
			for(int row = 0; row < dimension; row++) {
				for(int col = 0; col < dimension; col++) {
					if(type.isTile(col, row, 0)) {
						drawTile(type, startX + ((col - left) * TILE_SIZE), startY + ((row - top) * TILE_SIZE), g);
					}
				}
			}
		}
	}

	/**
	 * Draws a tile onto the preview window.
	 * @param type The type of tile to draw.
	 * @param x The x coordinate of the tile.
	 * @param y The y coordinate of the tile.
	 * @param g The graphics object.
	 */


	private void drawTile(TileType type, int x, int y, Graphics g) {
		/*
		 * Fill the entire tile with the base color.
		 */
		g.setColor(type.getBaseColor());
		g.fillRect(x, y, TILE_SIZE, TILE_SIZE);

		/*
		 * Fill the bottom and right edges of the tile with the dark shading color.
		 */
		g.setColor(type.getDarkColor());
		g.fillRect(x, y + TILE_SIZE - SHADE_WIDTH, TILE_SIZE, SHADE_WIDTH);
		g.fillRect(x + TILE_SIZE - SHADE_WIDTH, y, SHADE_WIDTH, TILE_SIZE);

		/*
		 * Fill the top and left edges with the light shading.
		 */
		g.setColor(type.getLightColor());
		for(int i = 0; i < SHADE_WIDTH; i++) {
			g.drawLine(x, y + i, x + TILE_SIZE - i - 1, y + i);
			g.drawLine(x + i, y, x + i, y + TILE_SIZE - i - 1);
		}
	}




}
