package utils;

public class Centroide {
    private double x;
    private double y;
    private int n;

    public Centroide() {
    }

    public Centroide(double x, double y, int n) {
        this.x = x;
        this.y = y;
        this.n = n;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    @Override
    public String toString() {
        return "Centroide{" + "x=" + x + ", y=" + y + ", n=" + n + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Centroide other = (Centroide) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return this.n == other.n;
    }
    
    
}
