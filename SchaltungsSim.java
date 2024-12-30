import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.awt.List;
import java.awt.Point;

enum Types{
    OR,AND,NOR,NAND,XOR,NOT
}

class Gate{
    String type;
    ArrayList<Boolean> inputs;
    Boolean output;
    ArrayList<Point> inputCoordinates = new ArrayList<>();
    Point[] outputCoordinates = new Point[1];
    String name = "";
    static int orCount;
    static int andCount;
    static int xorCount;
    static int norCount;
    static int nandCount;
    static int notCount;
    Gate(Types type){
        switch (type) {
            case Types.OR:
                this.type = "OR";
                orCount++;
                name = "OR" + orCount;
                break;
            case Types.AND:
                this.type = "AND";
                andCount++;
                name = "AND" + andCount;
                break;
            case Types.NOR:
                this.type = "NOR";
                norCount++;
                name = "NOR" + norCount;
                break;
            case Types.NAND:
                this.type = "NAND";
                nandCount++;
                name = "NAND" + nandCount;
                break;
            case Types.XOR:
                this.type = "XOR";
                xorCount++;
                name = "XOR" + xorCount;
                break;
            case Types.NOT:
                this.type = "NOT";
                notCount++;
                name = "NOT" + notCount;
                break;
            default:
                throw new IllegalArgumentException("Wrong type. Only (OR,AND,NAND,NOR,XOR,NOT) are available");
        }
        inputs = new ArrayList<>();
        this.output = null;
    }

    void addInput(Boolean bool){
        if(this.type.equals("NOT") && inputs.size() >= 1) throw new IllegalArgumentException("Ein NOT-Gatter hat nur einen Input");
        if(inputs.size() >= 2) throw new IllegalArgumentException("Jedes Gatter darf nur zwei inputs besitzen");
        inputs.add(bool);
    }

    void changeInput(int index){
        if(inputs.size() == 0) throw new IllegalArgumentException("Es muss mindestens ein Input vorhanden sein um diesen zu ändern");
        inputs.set(index, !inputs.get(index));
    }

    void calculateOutput(){
        if(this.type.equals("NOT") && inputs.size() < 1) throw new IllegalArgumentException("Nicht alle eingänge wurden belegt");
        else if(inputs.size() < 2) throw new IllegalArgumentException("Nicht alle eingänge wurden belegt");
        switch (type) {
            case "OR":
                output = inputs.stream().anyMatch(input -> input);
                break;
            case "AND":
                output = inputs.stream().allMatch(input -> input);
                break;
            case "NOT":
                output = !inputs.get(0);
                break;
            case "NAND":
                output = !inputs.stream().allMatch(input -> input);
                break;
            case "NOR":
                output = !inputs.stream().anyMatch(input -> input);
                break;
            case "XOR":
                output = inputs.get(0) ^ inputs.get(1);
                break;
        }
    }

    void getCoordInOut(int x, int y, Gate gate){
        
        if(gate.type.equals("NOT")){
            Point input1 = new Point(x-35,y);
            Point output = new Point(x+35,y);
            gate.inputCoordinates.add(input1);
            gate.outputCoordinates[0] = output;
        }else{
            Point input1 = new Point(x-35,y-10);
            Point input2 = new Point(x-35,y+10);
            Point output = new Point(x+35, y);
            gate.inputCoordinates.add(input1);
            gate.inputCoordinates.add(input2);
            gate.outputCoordinates[0] = output;
        }
        
    }

    
}



class Circuit<T>{
    String name;
    Map<Point,T> componentPoints;
    Turtle turtle;
    int row;
    int col;
    
    //Feld erstellen mit einer Standart Größe
    Circuit(String name){
        this(name, 6, 6);
    }
    // Zweiter Konstruktor falls der Benutzer sich direkt ein größers Feld erstellen möchte 
    Circuit(String name, int row, int col){
        assert row >= 3 && col >= 3: "row AND col must be >= 3";
        this.name = name;
        this.row = row;
        this.col = col;
        this.componentPoints = new HashMap<>();
        turtle = new Turtle(col*114,row*114);
        new FieldDraw().drawCircuitField();
    }
    private class FieldDraw{
        //Malt das Feld 
        void drawCircuitField(){
            turtle.reset();
            turtle.penDown();
            int x = 50;
            int y = 50;
            turtle.moveTo(x, y);
            turtle.color(168, 50, 50);
            for(int i = 1; i <= row; i++){
                for(int j = 1; j <= col; j++){
                    turtle.penDown();
                    drawSquare(100);
                    turtle.penUp();
                    turtle.forward(100);
                
                }
                turtle.moveTo(x,y+=100);
            }
            drawTop();
            drawLeft();
            drawRight();
            drawBottom();
            drawLettersTopBetter();
            drawLettersLeftBetter();
            turtle.right(90);
        }

        void drawBottom(){
            int x = 50;
            int y = (row * 100) + 50;
            turtle.color(61, 3, 252);
            for(int i = 0; i <= col; i++){
                turtle.moveTo(x, y);
                turtle.penDown();
                turtle.right(90);
                turtle.forward(50);
                turtle.backward(50);
                x += 100;
                turtle.left(90);
            }
        }

        /* 
        void drawLettersLeft(){
            int x = 18, y = 110;
            turtle.color(4, 1, 15);
            turtle.moveTo(x, y);
            for(int i = 1; i <= row; i++){
                turtle.text(String.valueOf(i), Font.ARIAL, 30, null);
                turtle.moveTo(x, y += 100);
            }
        }
        */

        void drawLettersLeftBetter(){
            int x = 18, y = 110;
            turtle.color(4,1,15).moveTo(x, y);
            IntStream.range(1, row+1).forEach(i->{turtle.text(String.valueOf(i), Font.ARIAL, 30, null).moveTo(x, (i*100)+y);});
        }
        void drawLettersTopBetter(){
            int x = 90, y = 30;
            turtle.color(4,1,15).moveTo(x, y).left(90).text("E", Font.ARIAL,30,null);
            IntStream.range(2, col+1).forEach(i -> {turtle.moveTo((90*i)+(i-1)*10, y).text(String.valueOf(i), Font.ARIAL, 30, null);});
        }
        /* 
        void drawLettersTop(){
            int x = 90, y = 30;
            turtle.color(4, 1, 15);
            turtle.moveTo(x, y);
            turtle.left(90);
            turtle.text("E", Font.ARIAL, 30, null);
            for(int i = 2; i <= col; i++){
                turtle.moveTo(x += 100, y);
                turtle.text(String.valueOf(i), Font.ARIAL, 30, null);
            }
        }
        */
        void drawRight(){
            int x = (col * 100) + 50, y = 50;
            turtle.color(61, 3, 252);
            for(int i = 0; i <= row; i++){
                turtle.moveTo(x, y);
                turtle.penDown();
                turtle.forward(50);
                turtle.backward(50);
                y += 100;
            }
        }

        void drawLeft(){
            int x = 50, y = 50;
            turtle.moveTo(x, y);
            turtle.color(61, 3, 252);
            for(int i = 0; i <= row; i++){
                turtle.penDown();
                turtle.left(180);
                turtle.forward(50);
                turtle.backward(50);
                turtle.penUp();
                turtle.moveTo(x, y += 100);
                turtle.right(180);
            }
        }

        void drawTop(){
            int y = 50, x = 50;
            turtle.moveTo(x, y);
            turtle.color(61, 3, 252);
            for(int j = 0; j <= col; j++){
                turtle.penDown();
                turtle.left(90);
                turtle.forward(50);
                turtle.backward(50);
                turtle.right(90);
                turtle.penUp();
                turtle.forward(100);
            }
            
        }
        void drawSquare(int size){
            turtle.penDown();
            for(int i = 1; i <= 4; i++){
                turtle.penDown();
                turtle.forward(size);
                turtle.right(90);
            }
        }
        
    }
    //um ein Feld zu erweitern
    void expandField(int row, int col){
        Clerk.clear();
        assert row > 0 || col > 0;
        assert row >= 0 && col >= 0: "its not allowed to choose a number < 0";
        this.row += row;
        this.col += col;
        this.turtle = new Turtle(114*this.col,114*this.row);
        new FieldDraw().drawCircuitField();
        drawAllComponents();
    }

    //Einfügen eines Gatters
    //Es ist darf auch nicht möglich sein in der ersten spalte ein Gate zu platziren da diese nur für eingänge sind 
    void drawAllComponents(){
        for(Map.Entry<Point, T> entry : componentPoints.entrySet()){
            Point point = entry.getKey();
            T component = entry.getValue();
            if(component instanceof Gate) {
                Gate gate = (Gate) component;
                drawGates(gate, point);
            }
        }
    }
    void addComponent(T component,int row,int col){
        int cellWidth = 100, cellHeight = 100;
        Point point = new Point(col,row);
        if(componentPoints.containsKey(point)) throw new IllegalArgumentException("their is already an component in this field");
        int y = row * cellHeight;
        int x = col * cellWidth;
        if(component instanceof Gate){
            Gate gate = (Gate) component;
            if(col == 1) throw new IllegalArgumentException("this column is only for inputs");
            gate.getCoordInOut(x, y, gate);
            drawGates(gate, point);
            componentPoints.put(point, component);
        }
    }
    //Methode um zwei Gatter mit einer Leitung zu verbinden
    void connectGates(int row, int col, int endRow, int endCol, int input){
        
    }
    
    
    

    
    void drawGates(Gate gate,Point point){
        switch (gate.type) {
            case "OR":
                drawOR(point.getX()*100, point.getY()*100);
                break;
            case "AND":
                drawAND(point.getX()*100, point.getY()*100);
                break;
            case "XOR":
                drawXOR(point.getX()*100, point.getY()*100);
                break;
            case "NOR":
                drawNOR(point.getX()*100, point.getY()*100);
                break;
            case "NAND":
                drawNAND(point.getX()*100, point.getY()*100);
                break;
            case "NOT":
                drawNOT(point.getX()*100, point.getY()*100);
                break;
            default:
                System.out.println("Wrong input");
                break;
        }
    }
    void drawCircle() {
        turtle.penDown();
        double radius = 5; // Radius von 5 Pixel
        double circumference = 2 * Math.PI * radius; // Berechnung des Umfangs
        double totalSteps = 50; // Anzahl der Schritte (mehr Schritte = glatterer Kreis)
        double stepSize = circumference / totalSteps; // Schrittgröße basierend auf dem Umfang
        double anglePerStep = 360.0 / totalSteps; // Winkel pro Schritt
    
        for (int i = 0; i < totalSteps; i++) {
            turtle.forward(stepSize).right(anglePerStep);
        }
    
        // Zurück zur ursprünglichen Ausrichtung
        turtle.left(360);
    }
    
    

    void drawSquareSquare(int size){
        turtle.penDown();
        for(int i = 1; i <= 4; i++){
            turtle.penDown();
            turtle.forward(size);
            turtle.right(90);
        }
    }

    void drawOutput(double x, double y){
        turtle.moveTo(x, y);
        turtle.penUp().forward(25).penDown().forward(10);
    }
    void drawInput(double x, double y){
        turtle.moveTo(x, y);
        turtle.penUp().left(180).forward(25).right(90).forward(10).left(90).penDown().forward(10).backward(10);
        turtle.penUp().left(90).forward(20).right(90).penDown().forward(10);
        turtle.right(180);
    }

    void drawOR(double x, double y){
        turtle.moveTo(x, y);
        turtle.left(90).penUp().forward(25).left(90).forward(25).right(180);
        drawSquareSquare(50);
        drawInput(x,y);
        drawOutput(x, y);
        turtle.moveTo(x,y).penUp().forward(4).left(90).forward(2).text("1", Font.ARIAL, 15, null);
        turtle.left(90).forward(2).penDown().forward(9).backward(9).penUp().right(90).left(90).forward(8).right(90).text(">", Font.ARIAL, 15, null).right(90);
    }
    void drawAND(double x, double y){
        turtle.moveTo( x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        drawOutput(x, y);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(5).right(90).text("&", Font.ARIAL, 15, null).right(90);
    }

    void drawNOR(double x, double y){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x,y).penUp().forward(4).left(90).forward(2).text("1", Font.ARIAL, 15, null);
        turtle.left(90).forward(2).penDown().forward(9).backward(9).penUp().right(90).left(90).forward(8).right(90).text(">", Font.ARIAL, 15, null).right(90);
    }   
    
    void drawNAND(double x, double y){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(5).right(90).text("&", Font.ARIAL, 15, null).right(90);
    }

    void drawXOR(double x, double y){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(9).right(90).text("=1", Font.ARIAL, 15, null).right(90);
    }

    void drawNOT(double x,double y){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        turtle.moveTo(x, y).penUp().backward(25).penDown().backward(10);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(4).right(90).text("1", Font.ARIAL, 15, null).right(90);
    }

}



class Wire{
    
}



//Circuit<Object> c = new Circuit<>("c");

