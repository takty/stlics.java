

public class Beta {

	static private double gamma(double a) {
		double t, x, y, u, r;
		if(a > 1) {
			t = Math.sqrt(2 * a - 1);
			do {
				do {
					do {
						do {
							x = Math.random();
							y = 2 * Math.random() - 1;
						} while((x * x + y * y >= 1) || (x == 0));
						y = y / x;
						x = t * y + a - 1;
					} while(x <= 0);
					u = (a - 1) * Math.log(x / (a - 1)) - t * y;
				} while(u <= -50);
			} while((1 + y * y) * Math.exp(u) <= Math.random());
		} else {
			t = Math.E / (a + Math.E);
			do {
				if(Math.random() < t) {
					x = 0;
					y = 1;
					r = Math.random();
					if(r > 0) {
						x = Math.exp(Math.log(r) / a);
						y = Math.exp(-x);
					}
				} else {
					r = Math.random();
					x = 1;
					y = 0;
					if(r > 0) {
						x = 1 - Math.log(r);
						y = Math.exp((a - 1) * Math.log(x));
					}
				}
			} while(Math.random() >= y);
		}
		return x;
	}

	static public double random(double a, double b) {
		double T = gamma(a);
		return T / (T + gamma(b));
	}

}
