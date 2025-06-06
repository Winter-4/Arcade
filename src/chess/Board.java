package chess;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class Board extends JPanel {
    
    public String fenStartingPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    
    //test FenStrings
    //StaleMate(King vs King and pawn)
    public String fenStaleMate = "7k/5K2/8/6P1/8/8/8/8 w - - 0 1";

    //Threefold Repetition
    public String fenThreefoldRepetition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    //50-move rule
    public String fenFiftyMoveRule = "8/8/8/8/8/8/8/8 w - - 100 101";

    //Insufficient Material
    public String fenInsufficientMaterial = "8/8/8/8/8/8/8/K1k5 w - - 0 1";

    //Checkmate
    public String fenCheckMate = "rnb1kbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 1 2";

    //En Passant Possible
    public String fenEnPassantPossible = "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 2";

    //Castling Rights Testing
    public String fenCastlingRights = "rnbq1bnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ - 0 1";

    //Promoted Pieces on Board
    public String fenPromotedPieces = "4k3/1Q6/8/8/8/8/6q1/4K3 w - - 0 1";

    //Dead Position
    public String fenDeadPosition = "8/8/8/8/8/8/6b1/5Bk1 w - - 0 1";

    public int halfmoveClock;
    public int fullmoveNumber;
    public String fenTest = "7r/8/2B5/5k2/8/3K4/8/8 w - - 0 1";

    public int tileSize = 85;

    int cols = 8;
    int rows = 8;

    ArrayList<Piece> pieceList = new ArrayList<Piece>();


    public Piece selectedPiece;

    Input input = new Input(this);

    public CheckScanner checkScanner = new CheckScanner(this);


    public int enPassantTile = -1;


    private boolean isWhiteToMove = true;
    private boolean isGameOver = false;

    public Board() {
        this.setPreferredSize(new Dimension(tileSize * cols, tileSize * rows));
        this.addMouseListener(input);
        this.addMouseMotionListener(input);

        loadPositionFromFEN(fenStartingPosition); 
        //loadPositionFromFEN(fenStaleMate); //StaleMate(King vs King and pawn)
        // loadPositionFromFEN(fenThreefoldRepetition); //Threefold Repetition
        // loadPositionFromFEN(fenFiftyMoveRule); //50-move rule
        // loadPositionFromFEN(fenInsufficientMaterial); //Insufficient Material
        // loadPositionFromFEN(fenCheckMate); //Checkmate
        // loadPositionFromFEN(fenEnPassantPossible); //En Passant Possible
        // loadPositionFromFEN(fenCastlingRights); //Castling Rights Testing\
        // loadPositionFromFEN(fenPromotedPieces); //Promoted Pieces on Board
        // loadPositionFromFEN(fenDeadPosition); //Dead Position
        //loadPositionFromFEN(fenTest);
        repaint();
    }

    public Piece getPiece(int col, int row) {
        for (Piece piece : pieceList) {
            if (piece.col == col && piece.row == row) {
                return piece;
            }
        }
        return null;
    }


    public void makeMove(Move move) {
        if (move.piece.name.equals("Pawn")){
            movePawn(move);
        } else if (move.piece.name.equals("King")){
            moveKing(move);
        } else {
            enPassantTile = -1;
        }
        if (move.piece.name.equals("King")){
            moveKing(move);
        }
        // Update halfmoveClock
        if (move.piece.name.equals("Pawn") || move.capture != null) {
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }

        // Draw by 50-move rule
        if (halfmoveClock >= 100) {
            System.out.println("Draw by 50-move rule.");
            isGameOver = true;
        }

        // Update fullmoveNumber (increment only after Black's move)
        if (!isWhiteToMove) {
            fullmoveNumber++;
        }


        move.piece.col = move.newCol;
        move.piece.row = move.newRow;
        move.piece.xPos = move.newCol * tileSize;
        move.piece.yPos = move.newRow * tileSize;
        
        move.piece.isFirstMove = false;
        
        capture(move.capture);

        isWhiteToMove = !isWhiteToMove;

        updateGameState();

        
    }


    private void moveKing(Move move) {
        if (Math.abs(move.piece.col - move.newCol) == 2){
            Piece rook;
            if (move.piece.col < move.newCol){
                rook = getPiece(7, move.piece.row);
                rook.col = 5;
            } else {
                rook = getPiece(0, move.piece.row);
                rook.col = 3;
            }

            rook.xPos = rook.col * tileSize;
        }
        move.piece.col = move.newCol;
        move.piece.row = move.newRow;
        move.piece.xPos = move.newCol * tileSize;
        move.piece.yPos = move.newRow * tileSize;
        
        move.piece.isFirstMove = false;
        
        capture(move.capture);
    }


    public void movePawn(Move move) {
        
        // en passant
        int colorIndex = move.piece.isWhite ? 1 : -1;

        if (getTileNum(move.newCol, move.newRow) == enPassantTile){
            move.capture = getPiece(move.newCol, move.newRow + colorIndex);
        }

        if (Math.abs(move.piece.row - move.newRow) == 2){
            enPassantTile = getTileNum(move.newCol, move.newRow + colorIndex);
        } else {
            enPassantTile = -1;
        }

        // promotions
        colorIndex = move.piece.isWhite ? 0 : 7;
        if (move.newRow == colorIndex){
            promotePawn(move);
        }
        
    }


    private void promotePawn(Move move) {
        // Create a dialog box to ask the player which piece to promote to
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};

        JOptionPane optionPane = new JOptionPane(
            "Choose a piece to promote to:",
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            options,
            options[0]
        );

        JDialog dialog = optionPane.createDialog(this, "Promotion");

        // Disable the close button (X)
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);

        // Get the selected value after dialog is closed
        Object selectedValue = optionPane.getValue();

        if (selectedValue == null) {
            System.out.println("No piece was selected.");
            return;
        }

        String choice = selectedValue.toString();  // Convert to string safely

        Piece promotedPiece = null;

        if (choice.equals("Queen")) {
            promotedPiece = new Queen(this, move.newCol, move.newRow, move.piece.isWhite);
        } else if (choice.equals("Rook")) {
            promotedPiece = new Rook(this, move.newCol, move.newRow, move.piece.isWhite);
        } else if (choice.equals("Bishop")) {
            promotedPiece = new Bishop(this, move.newCol, move.newRow, move.piece.isWhite);
        } else if (choice.equals("Knight")) {
            promotedPiece = new Knight(this, move.newCol, move.newRow, move.piece.isWhite);
        } else {
            System.out.println("Unrecognized promotion choice: " + choice);
            return;
        }

        // Replace pawn with promoted piece
        pieceList.remove(move.piece);
        pieceList.add(promotedPiece);

        repaint(); // Optional: if your board needs to be redrawn
    }



    public void capture(Piece piece) {
        pieceList.remove(piece);
    }


    public boolean isValidMove(Move move) {

        if (isGameOver){
            return false;
        }
        
        if (move.piece.isWhite != isWhiteToMove){
            return false;
        }
        if (sameTeam(move.piece, move.capture)){
            return false;
        }

        if (!move.piece.isValidMovement(move.newCol, move.newRow)){
            return false;
        }

        if (move.piece.moveCollidesWithPiece(move.newCol, move.newRow)){
            return false;
        }

        if (checkScanner.isKingChecked(move)){
            return false;
        }

        return true;
    }



    public boolean sameTeam(Piece p1, Piece p2) {
        if (p1 == null || p2 == null){
            return false;
        }
        return p1.isWhite == p2.isWhite;
    }


    Piece findKing(boolean isWhite){
        for (Piece piece : pieceList) {
            if (piece.name.equals("King") && piece.isWhite == isWhite){
                return piece;
            }
        }
        return null;
    }


    public int getTileNum(int col, int row) {
        return col + row * rows;
    }



    public void loadPositionFromFEN(String fenString){

        //System.out.println("added");
        pieceList.clear();
        String[] parts = fenString.split(" ");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid FEN string: must have 6 fields");
        }

        //set up pieces
        String position = parts[0];
        int row = 0;
        int col = 0;
        for (int i = 0; i < position.length(); i++) {
            char ch = position.charAt(i);
            if (ch == '/'){
                row++;
                col = 0;
            } else if (Character.isDigit(ch)){
                col += Character.getNumericValue(ch);
            } else {
                boolean isWhite = Character.isUpperCase(ch);
                char pieceChar = Character.toLowerCase(ch);
                switch (pieceChar) {
                    case 'r':
                        pieceList.add(new Rook(this, col, row, isWhite));
                        break;
                    case 'n':
                        pieceList.add(new Knight(this, col, row, isWhite));
                        break;
                    case 'b':
                        pieceList.add(new Bishop(this, col, row, isWhite));
                        break;
                    case 'q':
                        pieceList.add(new Queen(this, col, row, isWhite));
                        break;
                    case 'k':
                        pieceList.add(new King(this, col, row, isWhite));
                        break;
                    case 'p':
                        pieceList.add(new Pawn(this, col, row, isWhite));
                        break;
                }
                col++;
            }
        }

        // color to move
        isWhiteToMove = parts[1].equals("w");

        // castling rights
        Piece bqr = getPiece(0, 0);
        if (bqr instanceof Rook){
            bqr.isFirstMove = parts[2].contains("q");
        }
        Piece bkr = getPiece(0, 0);
        if (bkr instanceof Rook){
            bkr.isFirstMove = parts[2].contains("k");
        }
        Piece wqr = getPiece(0, 0);
        if (wqr instanceof Rook){
            wqr.isFirstMove = parts[2].contains("Q");
        }
        Piece wkr = getPiece(0, 0);
        if (wkr instanceof Rook){
            wkr.isFirstMove = parts[2].contains("K");
        }

        // en passant
        if (parts[3].equals("-")){
            enPassantTile = -1;
        } else {
            enPassantTile = (7 - (parts[3].charAt(1) - '1')) * 8 + (parts[3].charAt(0) - 'a');
        }


        // halfmoveClock
        halfmoveClock = Integer.parseInt(parts[4]);

        // fullmoveNumber
        fullmoveNumber = Integer.parseInt(parts[5]);

        
    }

    private void updateGameState() {
        Piece king = findKing(isWhiteToMove);

        if (checkScanner.isGameOver(king)){
            if (checkScanner.isKingChecked(new Move(this, king, king.col, king.row))) {
                System.out.println(isWhiteToMove ? "Black wins!" : "White wins!");
            } else {
                System.out.println("Stalemate!");
            }
            isGameOver = true;
        } else if (insufficientMaterial(true) && insufficientMaterial(false)){
            System.out.println("Stalemate! Insufficient material.");
            isGameOver = true;
        }
    }

    private boolean insufficientMaterial(boolean isWhite) {
        ArrayList<String> names = pieceList.stream().filter(p -> p.isWhite == isWhite).map(p -> p.name).collect(Collectors.toCollection(ArrayList::new));
        if (names.contains("Queen") || names.contains("Rook") || names.contains("Pawn")){
            return false;
        }
        return names.size() < 3;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        //paint board
        for (int r = 0; r<rows; r++) {
            for (int c = 0; c<cols; c++) {
                if ((r + c) % 2 == 0) {
                    g2d.setColor(new Color(227, 198, 181));
                } else {
                    g2d.setColor(new Color(157, 105, 53));
                }
                g2d.fillRect(c*tileSize, r*tileSize, tileSize, tileSize);
            }
        }

        //paint highlights
        if(selectedPiece != null){
            for (int r = 0; r<rows; r++) {
                for (int c = 0; c<cols; c++) {
                    if(isValidMove(new Move(this, selectedPiece, c, r))){
                        g2d.setColor(new Color(68, 180, 57, 190));
                        g2d.fillRect(c*tileSize, r*tileSize, tileSize, tileSize);
                    }
                }
            }
        }
        
        //paint pieces
        //System.out.println("Piece list size: " + pieceList.size());
        for (Piece piece : pieceList) {
            //System.out.println(piece);
            if (piece != selectedPiece){
                piece.paint(g2d);
            }
        }
        if (selectedPiece != null) {
            selectedPiece.paint(g2d);
        }


    }

}
