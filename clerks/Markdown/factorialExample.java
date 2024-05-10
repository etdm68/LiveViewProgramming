
// Fakultätsfunktion
long factorial(int n) {
    assert n >= 0 : "Positive Ganzzahl erforderlich";
    if (n == 0 || n == 1) return n;
    return n * factorial(n - 1);
}
// Testfälle
assert factorial(0) == 1 && factorial(1) == 1;
assert factorial(2) == 2 && factorial(3) == 6;
assert factorial(4) == 24 && factorial(5) == 120;