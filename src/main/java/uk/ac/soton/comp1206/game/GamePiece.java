package uk.ac.soton.comp1206.game;

/**
 * Instances of GamePiece Represents the model of a specific Game Piece with it's block makeup.
 *
 * The GamePiece class also contains a factory for producing a GamePiece of a particular shape, as specified by it's
 * number.
 */
public class GamePiece {
    /**
     * The total number of pieces in this game
     */
    public static final int PIECES = 15;

    /**
     * The 2D grid representation of the shape of this piece
     */
    private int[][] blocks;

    /**
     * The value of this piece
     */
    private final int value;

    /**
     * The name of this piece
     */
    private final String name;


    /**
     * Create a new GamePiece with the given name, block makeup and value. Should not be called directly, only via the
     * factory.
     * @param name name of the piece
     * @param blocks block makeup of the piece
     * @param value the value of this piece
     */
    private GamePiece(String name, int[][] blocks, int value) {
        this.name = name;
        this.blocks = blocks;
        this.value = value;

        for(int x = 0; x < blocks.length; ++x) {
            for(int y = 0; y < blocks[x].length; ++y) {
                if (blocks[x][y] != 0) {
                    blocks[x][y] = value;
                }
            }
        }

    }

    /**
     * Get the value of this piece
     * @return piece value
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Get the block makeup of this piece
     * @return 2D grid of the blocks representing the piece shape
     */
    public int[][] getBlocks() {
        return this.blocks;
    }

    /**
     * Rotate this piece the given number of rotations
     * @param rotations number of rotations
     */
    public void rotate(int rotations) {
        for(int rotated = 0; rotated < rotations; ++rotated) {
            this.rotate();
        }

    }

    /**
     * Rotate this piece exactly once by rotating it's 3x3 grid
     */
    public void rotate() {
        int[][] rotated = new int[this.blocks.length][this.blocks[0].length];
        rotated[2][0] = this.blocks[0][0];
        rotated[1][0] = this.blocks[0][1];
        rotated[0][0] = this.blocks[0][2];
        rotated[2][1] = this.blocks[1][0];
        rotated[1][1] = this.blocks[1][1];
        rotated[0][1] = this.blocks[1][2];
        rotated[2][2] = this.blocks[2][0];
        rotated[1][2] = this.blocks[2][1];
        rotated[0][2] = this.blocks[2][2];
        this.blocks = rotated;
    }

    /**
     * Create a new GamePiece of the specified piece number and rotation
     * @param piece piece number
     * @param rotation number of times to rotate
     * @return the created GamePiece
     */
    public static GamePiece createPiece(int piece, int rotation) {
        GamePiece newPiece = createPiece(piece);
        newPiece.rotate(rotation);
        return newPiece;
    }

    /**
     * Return the string representation of this piece
     * @return the name of this piece
     */
    public String toString() {
        return this.name + " (" + this.value + ")";
    }

    /**
     * Create a new GamePiece of the specified piece number
     * @param piece piece number
     * @return the created GamePiece
     */
    public static GamePiece createPiece(int piece) {
        int[][] blocks;
        switch(piece) {
            case 0:
                blocks = new int[][]{{0, 0, 0}, {1, 1, 1}, {0, 0, 0}};
                return new GamePiece("Line", blocks, 1);
            case 1:
                blocks = new int[][]{{0, 0, 0}, {1, 1, 1}, {1, 0, 1}};
                return new GamePiece("C", blocks, 2);
            case 2:
                blocks = new int[][]{{0, 1, 0}, {1, 1, 1}, {0, 1, 0}};
                return new GamePiece("Plus", blocks, 3);
            case 3:
                blocks = new int[][]{{0, 0, 0}, {0, 1, 0}, {0, 0, 0}};
                return new GamePiece("Dot", blocks, 4);
            case 4:
                blocks = new int[][]{{1, 1, 0}, {1, 1, 0}, {0, 0, 0}};
                return new GamePiece("Square", blocks, 5);
            case 5:
                blocks = new int[][]{{0, 0, 0}, {1, 1, 1}, {0, 0, 1}};
                return new GamePiece("L", blocks, 6);
            case 6:
                blocks = new int[][]{{0, 0, 1}, {1, 1, 1}, {0, 0, 0}};
                return new GamePiece("J", blocks, 7);
            case 7:
                blocks = new int[][]{{0, 0, 0}, {0, 1, 1}, {1, 1, 0}};
                return new GamePiece("S", blocks, 8);
            case 8:
                blocks = new int[][]{{1, 1, 0}, {0, 1, 1}, {0, 0, 0}};
                return new GamePiece("Z", blocks, 9);
            case 9:
                blocks = new int[][]{{1, 0, 0}, {1, 1, 0}, {1, 0, 0}};
                return new GamePiece("T", blocks, 10);
            case 10:
                blocks = new int[][]{{1, 0, 1}, {0, 1, 0}, {1, 0, 1}};
                return new GamePiece("X", blocks, 11);
            case 11:
                blocks = new int[][]{{0, 0, 0}, {1, 1, 0}, {1, 0, 0}};
                return new GamePiece("Corner", blocks, 12);
            case 12:
                blocks = new int[][]{{1, 0, 0}, {1, 1, 0}, {0, 0, 0}};
                return new GamePiece("Inverse Corner", blocks, 13);
            case 13:
                blocks = new int[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
                return new GamePiece("Diagonal", blocks, 14);
            case 14:
                blocks = new int[][]{{0, 1, 0}, {0, 1, 0}, {0, 0, 0}};
                return new GamePiece("Double", blocks, 15);
            default:
                throw new IndexOutOfBoundsException("No such piece: " + piece);
        }
    }
}
