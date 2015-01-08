package nl.totan.util;

import lejos.nxt.LCD;
import lejos.util.Matrix;

public class Vector {
	protected static final int dimension=3;
	protected float[] v;
	
	public Vector( float initial) {
		v=new float[dimension];
		for (int i=0;i<dimension;i++) v[i]=initial;
	}
	
	public Vector() {
		this(0);
	}
	
	public Vector(float[] a) {
		v= a.clone();
	}
	
	public Vector(float x, float y, float z) {
		v=new float[dimension];
		v[0]=x;
		v[1]=y;
		v[2]=z;
	}
	
	public Vector(Matrix m, int rowcol, int index) {
		this();
		for (int i=0;i<dimension;i++) {
			if (rowcol==0) {
				v[i]=(float)m.get(index, i);
			}
			else {
				v[i]=(float)m.get(i, index);
			}
		}
	}
	
	public float get(int r) {
		return v[r];
	}
	
	public void set(int r, float value) {
		v[r]=value;
	}
	
	public void setArray(float[] v) {
		this.v=v;
	}
	
	public float[] getArray() {
		return v;
	}
	
	public void set(float[] a) {
		for (int i=0;i<dimension;i++) {
			v[i]=a[i];
		}
		
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public Vector copy() {
		return new Vector(this.v);
	}
	
	public Vector cross(Vector a) {
		Vector r=new Vector(dimension);
		r.v[0]=v[1]*a.v[2]-v[2]*a.v[1];
		r.v[1]=v[2]*a.v[0]-v[0]*a.v[2];
		r.v[2]=v[0]*a.v[1]-v[1]*a.v[0];
		return r; 
	}
	
	public float dot(Vector a) {
		float result=0;
		for (int i=0;i<dimension;i++) {
			result += a.v[i]*v[i];
		}
		return result;
	}
	
	public float angle (Vector a) {
		return (float)Math.acos(productOfElements(a)/((a.length()*length())));
		
		
	}
	
	public float length(){
		return (float)Math.sqrt(lengthSquared());
	}
	
	public float lengthSquared(){
		float result=0;
		for (int i=0;i<dimension;i++) {
			result += v[i]*v[i];
		}
		return result;
	}

	
	public float productOfElements(Vector a) {
		float result=0;
		for (int i=0;i<dimension;i++) {
			result = a.v[i]*v[i];
		}
		return result;
	}
	
	
	

	public void productEquals(Vector a) {
		for (int i=0;i<dimension;i++) {
			v[i] *= a.v[i];
		}
	}
	
	public void productEquals(float p) {
		for (int i=0;i<dimension;i++) {
			v[i] *= p;
		}
	}
	
	public Vector product(float f) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i] *= v[i]*f;
		}
		return r;
	}
	
	
	public Vector product(Vector a) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i] = v[i]*a.v[i];
		}
		return r;
	}
	
	public Vector product(Matrix a) {
		float t;
		Vector r=new Vector();
		for (int i=0;i<3;i++) {
			t=0;
			for (int j=0;j<3;j++)
				t+=a.get(i, j)*v[j];
			r.v[i]=t;
		}
		return r;
	}
	
	

	public void devideEquals(Vector a) {
		for (int i=0;i<dimension;i++) {
			v[i] /= a.v[i];
		}
	}
	
	public void devideEquals(float p) {
		for (int i=0;i<dimension;i++) {
			v[i] /= p;
		}
	}
	
	public Vector devide(Vector a) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i] = v[i]/a.v[i];
		}
		return r;
	}

	public Vector devide(float p) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i]=v[i] / p;
		}
		return r;
	}

	
	public void sumEquals(Vector a) {
		for (int i=0;i<dimension;i++) {
			v[i] += a.v[i];
		}
	}
	
	public void sumEquals(float p) {
		for (int i=0;i<dimension;i++) {
			v[i] += p;
		}
	}
	
	public Vector sum(Vector a) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i] = v[i]+a.v[i];
		}
		return r;
	}

	public Vector sum(float p) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i]=v[i] + p;
		}
		return r;
	}

	public void substractEquals(Vector a) {
		for (int i=0;i<dimension;i++) {
			v[i] -= a.v[i];
		}
	}
	
	public void substractEquals(float p) {
		for (int i=0;i<dimension;i++) {
			v[i] -= p;
		}
	}
	
	public Vector substract(Vector a) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i] = v[i]-a.v[i];
		}
		return r;
	}

	public Vector substract(float p) {
		Vector r=new Vector(dimension);
		for (int i=0;i<dimension;i++) {
			r.v[i]=v[i] - p;
		}
		return r;
	}


	public void normalize() {
		this.devide(this.length());
	}
	
	
	public Matrix convertToSkewSymmetric() {
		Matrix r=new Matrix(3,3,0);
		r.set(0, 1, -v[2]);
		r.set(0, 2, v[1]);
		r.set(1, 0, v[2]);
		r.set(1, 2, -v[0]);
		r.set(2, 0, -v[1]);
		r.set(2, 1, v[0]);
		return r;
	}
		
	public void show() {
		show(0,0);
		
	}

	public void show(int x, int y) {
		for  (int i=0;i<dimension;i++) {
			LCD.drawString(Formatter.format(v[i],2,3), x, y+i);
		}
		
	}



}
