import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.awt.List;
import java.awt.Point;

enum Types{
    OR,AND,NOR,NAND,XOR,NOT
}
enum HiLo{
    HIGH(1),
    LOW(0);
    final int value;
    private HiLo(int value){
        this.value = value;
    }
}

class Gate{
    String type;
    ArrayList<Integer> inputs;
    int output;
    ArrayList<Point> inputCoordinates = new ArrayList<>();
    Point[] outputCoordinates = new Point[1];
    ArrayList<Object> connections = new ArrayList<>();
    String name = "";
    static int orCount, andCount,xorCount,norCount,nandCount,notCount;
    Point gatePosition;
    Gate(Types type, int row,int col){
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
        this.gatePosition = new Point(col,row);
        this.output = 0;
    }

    void addGateInput(Input input){
        if(this.type.equals("NOT") && inputs.size() >= 1) throw new IllegalArgumentException("Ein NOT-Gatter hat nur einen Input");
        if(inputs.size() >= 2) throw new IllegalArgumentException("Jedes Gatter darf nur zwei inputs besitzen");
        inputs.add(input.value);
    }

    void changeInput(int index){
        assert index == 0 || index == 1;
        if(inputs.size() == 0) throw new IllegalArgumentException("Es muss mindestens ein Input vorhanden sein um diesen zu ändern");
        inputs.set(index, ~inputs.get(index));
    }

    void calculateOutput(){
        if(this.type.equals("NOT") && inputs.size() < 1) throw new IllegalArgumentException("Nicht alle eingänge wurden belegt");
        else if(inputs.size() < 2) throw new IllegalArgumentException("Nicht alle eingänge wurden belegt");
        switch (type) {
            case "OR":
                output = inputs.get(0) | inputs.get(1);
                break;
            case "AND":
                output = inputs.get(0) & inputs.get(1);
                break;
            case "NOT":
                output = ~inputs.get(0);
                break;
            case "NAND":
                output = ~(inputs.get(0) & inputs.get(1));
                break;
            case "NOR":
                output = ~(inputs.get(0) | inputs.get(1));
                break;
            case "XOR":
                output = inputs.get(0) ^ inputs.get(1);
                break;
        }
    }

    void getCoordInOut(double x, double y, Gate gate){
        
        if(gate.type.equals("NOT")){
            Point input1 = new Point((int)x-30,(int)y);
            Point output = new Point((int)x+35,(int)y);
            gate.inputCoordinates.add(input1);
            gate.outputCoordinates[0] = output;
        }else{
            Point input1 = new Point((int)x-30,(int)y-10);
            Point input2 = new Point((int)x-30,(int)y+10);
            Point output = new Point((int)x+35,(int)y);
            gate.inputCoordinates.add(input1);
            gate.inputCoordinates.add(input2);
            gate.outputCoordinates[0] = output;
        }
        
    }
}


//Mit der Circuit Klasse wird das Programm betrieben
class Circuit{
    String name;
    ArrayList<Gate> gates;
    ArrayList<Input> inputs;
    ArrayList<Output> outputs;
    Turtle turtle;
    int row;
    int col;
    int dynamicOffset = 5;
    private ArrayList<Integer> offsets = new ArrayList<>();
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
        this.gates = new ArrayList<>();
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        turtle = new Turtle(col*114,row*114);
        for(int i = 1; i <= col; i++){
            offsets.add(5);
        }
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
        outputs.forEach(output -> output.col += col);
        drawAllComponents();
        drawAllInputs();
        drawAllOutputs();
    }
    //Methode zum Einfügen von Inputs
    Circuit addInput(int row,HiLo hilo){
        assert row <= this.row && row>0;
        Input input = new Input(row, hilo);
        if(inputs.stream().anyMatch(inputs -> inputs.row == input.row)) throw new IllegalArgumentException("Their is already a Input on this position");
        inputs.add(input);
        drawInputE(1*100, row*100, input);
        return this;
    }
    //Methode um die Werte der Inputs zu ändern
    void setInput(int row, HiLo hilo){
        assert row <= this.row && row > 0;
        Clerk.clear();
        Input in = inputs.stream().filter(input -> input.row == row).findFirst().orElseThrow(() -> new IllegalArgumentException("Their is no Input in this row"));
        in.setValue(hilo);
        this.turtle = new Turtle(114*this.col,114*this.row);
        new FieldDraw().drawCircuitField();
        drawAllInputs();
        drawAllComponents();
    }

    //Wird benötigt um nachdem ändern eines Inputs das Feld zu verändern
    void drawAllInputs(){
        inputs.forEach(input -> drawInputE(1*100, input.row*100, input));
    }
    void drawAllComponents(){
        gates.forEach(gates -> drawGates(gates, gates.gatePosition));
    }
    void drawAllOutputs(){
        outputs.forEach(output -> drawOutputA(col*100, output.row*100, output));
    }
    Circuit addOutput(int row){
        assert row <= this.row && row > 0;
        if(outputs.stream().anyMatch(output -> output.row == row))throw new IllegalArgumentException("Their is already an output on this position");
        Output output = new Output(this.col, row);
        outputs.add(output);
        drawOutputA(col*100, row*100, output);
        return this;
    }
    //Methode um Gatter hinzuzufügen
    Circuit addComponent(Gate gate){
        if(gate.gatePosition.getX() == 1 || gate.gatePosition.getX() == col) throw new IllegalArgumentException("Its not possible to place a Gate on this position");
        if(gates.stream().anyMatch(gates -> gates.gatePosition.equals(gate.gatePosition))) throw new IllegalArgumentException("Their is already a Gate on this position");
        else {
            gates.add(gate);
            drawGates(gate, gate.gatePosition);
        }
        return this;
    }
    //Input mit einem Gatter verbinden
    void connectInGate(int row, String gatename){
        assert row <= this.row;
        Input input = inputs.stream().filter(input1 -> input1.row == row).findFirst().orElseThrow(() -> new IllegalArgumentException("Their is no Input in this row"));
        Gate gate = gates.stream().filter(gate1 -> gate1.name.equals(gatename)).findFirst().orElseThrow(() -> new IllegalArgumentException("Their is no Gate with this name"));
        input.connectionsToGates.add(gate);
        gate.addGateInput(input);
        drawConnInToGate(input, gate);
    }
    //Methode um zwei Gatter mit einer Leitung zu verbinden
    void connectGates(String start, String end){
        Gate startGate = gates.stream().filter(gate -> gate.name.equals(start)).findFirst().orElseThrow(() -> new IllegalArgumentException("Their is no Gate with this name: " + start));
        
        Gate endGate = gates.stream()
            .filter(gate -> gate.name.equals(end))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Their is no gate with this name: " + end));

        if(endGate.inputs.size() == 2 || endGate.type.equals("NOT") && endGate.inputs.size() == 1) throw new IllegalArgumentException("The inputs of the gate are already occupied");
        endGate.inputs.add(startGate.output);
        startGate.connections.add(endGate);
        drawConnGateToGate(startGate, endGate);
    }
    //Input darf nur als start verwendet werden
    //Output darf nur als end verwendet werden
    
    void connect(String start, String end){
        if(start.equals(end)) throw new IllegalArgumentException("It is not allowed to connect a Component with itself");
        Object startComponent = findStartComponent(start);
        Object endComponent = findEndComponent(end);
        //Pattern Matching
        switch (startComponent) {
            case Gate startGate : 
                if(endComponent instanceof Gate endGate){
                    if(endGate.inputs.size() == 2 || endGate.type.equals("NOT") && 
                    endGate.inputs.size() == 1) throw new IllegalArgumentException("The inputs of this gate are already occupied");
                    
                    endGate.inputs.add(startGate.output);
                    startGate.connections.add(endGate);
                    drawConnGateToGate(startGate, endGate);
                }
                else if(endComponent instanceof Output output){
                    output.setConnection(startGate.output);
                    startGate.connections.add(output);
                }
                else throw new IllegalArgumentException("Wrong Component");
                break;
            case Input input :
                if(endComponent instanceof Gate endGate){
                    if(endGate.inputs.size() == 2 || endGate.type.equals("NOT") && 
                    endGate.inputs.size() == 1) throw new IllegalArgumentException("The inputs of this gate are already occupied");

                    endGate.inputs.add(input.value);
                    input.connectionsToGates.add(endGate);
                    drawConnInToGate(input, endGate);
                    break;
                }
                else if(endComponent instanceof Output) throw new IllegalArgumentException("It is not allowed to connect an input with an output");
                else throw new IllegalArgumentException("Wrong Component");
            default:
                throw new IllegalArgumentException("Incorrect values were passed");
                
        }
        
    }
        
    Object findEndComponent(String end){

        //Optional<Gate> 
        for(Gate gate : gates){
            if(gate.name.equals(end)){
                return gate;
            }
        }
        for(Output output : outputs){
            if(output.name.equals(end)){
                return output;
            }
        }
        throw new IllegalArgumentException("Their is no component with this name");
    }
    Object findStartComponent(String start){
        for(Gate gate : gates){
            if(gate.name.equals(start)){
                return gate;
            }
        }
        for(Input input : inputs){
            if(input.name.equals(start)){
                return input;
            }
        }
        throw new IllegalArgumentException("Their is no component with this name");
    }
    
    
    double[] checkIfGateIsNearByX(double x, double y, String start, String end){
        double[] result = new double[2];
        for(Gate gate : gates){
            if(((gate.gatePosition.getX()*100)-40) - x == 0 && (gate.gatePosition.getY()*100) - y == 0){
                if(gate.name.equals(start) || gate.name.equals(end)){
                    result[0] = x;
                    result[1] = y;
                    return result;
                }
                else {
                    turtle.right(90)
                    .forward(30)
                    .left(90)
                    .forward(70);
                    result[0] = x + 70;
                    result[1] = y + 30;
                    return result;
                }
            }
        }
        result[0] = x;
        result[1] = y;

        /* 
        double[] yTemp = {y};
        double[] xTemp = {x};
        double[] result = new double[2];
        gates.forEach(gate -> {
            if((gate.gatePosition.getX()-40) - x == 0 && gate.gatePosition.getY() - y == 0){
                if(gate.name.equals(start) || gate.name.equals(end)){
                    result[0] = xTemp[0];
                    result[1] = yTemp[0];
                    
                }
                else {
                    turtle.right(90)
                    .forward(30)
                    .left(90)
                    .forward(70);
                    yTemp[0] = yTemp[0] + 30;
                    xTemp[0] = xTemp[0] + 70;
                    result[0] = xTemp[0];
                    result[1] = yTemp[0];
                }
            }
        });
        */
        return result;
    }
    
    void drawConnectionToOutput(){

    }
    void drawConnections(double xStart, double yStart, double xEnd, double yEnd, String start, String end){
        
        turtle.moveTo(xStart, yStart);
        while(xStart != xEnd-dynamicOffset){
            System.out.println(xStart + " " + yStart);
            double[] result = checkIfGateIsNearByX(xStart, yStart, start, end);
            xStart = result[0];
            yStart = result[1];
            turtle.penDown().forward(1);
            xStart++;
        }
        if(yStart < yEnd){
            turtle.right(90);
            while(yStart != yEnd){
                turtle.penDown().forward(1);
                yStart++;
            }
            turtle.left(90).forward(dynamicOffset);
        }
        else{
            turtle.left(90);
            while (yStart != yEnd) {
                turtle.penDown().forward(1);
                yStart--;
            }
            turtle.right(90).forward(dynamicOffset);
        }
        dynamicOffset += 5;
    }
    void drawConnGateToGate(Gate startGate, Gate endGate){
        double startXPos = startGate.outputCoordinates[0].getX(), startYPos = startGate.outputCoordinates[0].getY();
        int inputsSize = endGate.inputs.size();
        double endXPos, endYPos;
        System.out.println(inputsSize);
        endXPos = inputsSize == 1 ? endGate.inputCoordinates.get(0).getX() : endGate.inputCoordinates.get(1).getX(); 
        endYPos = inputsSize == 1 ? endGate.inputCoordinates.get(0).getY() : endGate.inputCoordinates.get(1).getY(); 
        drawConnections(startXPos, startYPos, endXPos, endYPos, startGate.name, endGate.name);
    }


    void drawConnInToGate(Input input, Gate gate){
        double startXPos = input.col * 100, startYPos = input.row * 100;
        int inputsSize = gate.inputs.size();
        double gateXPos, gateYPos;
        gateXPos = inputsSize == 1? gate.inputCoordinates.get(0).getX() : gate.inputCoordinates.get(1).getX();
        gateYPos = inputsSize == 1? gate.inputCoordinates.get(0).getY() : gate.inputCoordinates.get(1).getY();
        turtle.moveTo(startXPos, startYPos);
        drawConnections(startXPos, startYPos, gateXPos, gateYPos, input.name, gate.name);
    }

    void drawGates(Gate gate,Point point){
        switch (gate.type) {
            case "OR":
                drawOR(point.getX()*100, point.getY()*100, gate);
                break;
            case "AND":
                drawAND(point.getX()*100, point.getY()*100, gate);
                break;
            case "XOR":
                drawXOR(point.getX()*100, point.getY()*100, gate);
                break;
            case "NOR":
                drawNOR(point.getX()*100, point.getY()*100, gate);
                break;
            case "NAND":
                drawNAND(point.getX()*100, point.getY()*100, gate);
                break;
            case "NOT":
                drawNOT(point.getX()*100, point.getY()*100, gate);
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
        turtle.penUp().left(180).forward(25).right(90).forward(10).left(90).penDown().forward(5).backward(5);
        turtle.penUp().left(90).forward(20).right(90).penDown().forward(5);
        turtle.right(180);
    }

    void drawGateName(double x, double y, Gate gate){
        turtle.moveTo(x, y)
        .right(90)
        .penUp().forward(10)
        .right(90).forward(14)
        .right(90)
        .text(gate.name, Font.COURIER, 11, null).right(90);
        //turtle.moveTo(x, y).left(90).penUp().forward(30).left(90).forward(20).right(90).text(gate.name, Font.ARIAL, 15, null).right(90);
    }

    void drawOR(double x, double y, Gate gate){
        turtle.moveTo(x, y);
        turtle.left(90).penUp().forward(25).left(90).forward(25).right(180);
        drawSquareSquare(50);
        drawInput(x,y);
        drawOutput(x, y);
        turtle.moveTo(x,y).penUp().forward(4).left(90).forward(2).text("1", Font.ARIAL, 15, null);
        turtle.left(90).forward(2).penDown().forward(9).backward(9).penUp().right(90).left(90).forward(8).right(90).text(">", Font.ARIAL, 15, null).right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }
    void drawAND(double x, double y, Gate gate){
        turtle.moveTo( x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        drawOutput(x, y);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(5).right(90).text("&", Font.ARIAL, 15, null).right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    void drawNOR(double x, double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x,y).penUp().forward(4).left(90).forward(2).text("1", Font.ARIAL, 15, null);
        turtle.left(90).forward(2).penDown().forward(9).backward(9).penUp().right(90).left(90).forward(8).right(90).text(">", Font.ARIAL, 15, null).right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }   
    
    void drawNAND(double x, double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(5).right(90).text("&", Font.ARIAL, 15, null).right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    void drawXOR(double x, double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(9).right(90).text("=1", Font.ARIAL, 15, null).right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    void drawNOT(double x,double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        turtle.moveTo(x, y).penUp().backward(25).penDown().backward(10);
        turtle.moveTo(x, y).penUp().forward(31).left(90).forward(5).right(90);
        drawCircle();
        turtle.moveTo(x, y).penUp().forward(35).penDown().forward(6).backward(6);
        turtle.moveTo(x, y).left(90).penUp().forward(10).left(90).forward(4).right(90).text("1", Font.ARIAL, 15, null).right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    void drawInputE(double x, double y, Input input){
        turtle.moveTo(x-35, y-20)
        .left(90)
        .text(input.name, Font.ARIAL,15,null)
        .right(90);
        if(input.value == 1){
            turtle.color(11, 128, 39);
        }
        else turtle.color(10, 10, 10);
        turtle.moveTo(x, y)
        .left(180)
        .penDown()
        .forward(20)
        .penUp().forward(15)
        .right(90)
        .text(String.valueOf(input.value), Font.ARIAL, 15, null)
        .forward(20)
        .right(90)
        .color(10, 10, 10);
    }
    void drawOutputA(double x, double y, Output output){
        if(output.value == 1){
            turtle.color(11,128,39);
        }
        else turtle.color(10,10,10);
        turtle.moveTo(x, y)
        .penDown()
        .forward(20)
        .penUp().forward(10)
        .left(90)
        .text(String.valueOf(output.value), Font.ARIAL, 15, null)
        .forward(15)
        .left(90)
        .forward(15)
        .right(90)
        .text(output.name, Font.ARIAL, 15, null)
        .color(10,10,10)
        .right(90);
    }
}

class Input{
    int col, row, value;
    String name;
    ArrayList<Gate> connectionsToGates = new ArrayList<>();
    Input(int row, HiLo value){
        this.row = row;
        this.col = 1;
        this.value = value.value;
        this.name = "E" + row;
    }

    void setValue(HiLo value){
        this.value = value.value;
    }

    int getValue(){
        return value;
    }
}

class Output{
    boolean hasConnection = false;
    String name;
    int col, row, value;
    Output(int col, int row){
        this.col = col;
        this.row = row;
        this.value = 0;
        this.name = "A" + row;
    }

    boolean setConnection(int value){
        if(!hasConnection){
            this.value = value;
            return true;
        }
        return false;
    }

    void setValue(int value){
        this.value = value;
    }
    
}

class Wire{
    String start;
    String end;
    Wire(String start, String end){
        this.start = start;
        this.end = end;
    }
    
}



/*
 * Bermerkungen für mich um später den Code nochmal anzupassen:
 * In der getCoordinateInOut muss ich wahrscheinlich die Koordinaten nochmal ändern um einen ansehnlicheren Code zu erhalten
 * Generel auch nochmal den gesamten Code nach verbesserungen absuchen um moderneren Code zu erhalten
 */
