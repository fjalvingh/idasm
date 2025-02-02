package to.etc.dec.idasm;

public class Tmp {
	static public void main(String[] args) throws Exception {
		run("SISD", "Supervisor I space PDR#", 0772200);
		run("SDSD", "Supervisor D space PDR#", 0772220);

		run("SISA", "Supervisor I space PAR#", 0772240);
		run("SDSA", "Supervisor D space PAR#", 0772260);

		run("KISD", "Kernel I space PDR#", 0772300);
		run("KDSD", "Kernel D space PDR#", 0772320);

		run("KISA", "Kernel I space PAR#", 0772340);
		run("KDSA", "Kernel D space PAR#", 0772360);

		run("UISD", "User I space PDR#", 0777600);
		run("UDSD", "User D space PDR#", 0777620);

		run("UISA", "User I space PAR#", 0777640);
		run("UDSA", "User D space PAR#", 0777660);
	}

	private static void run(String label, String desc, int from) {
		for(int i = 0; i < 8; i++) {
			System.out.println(
				"0" + Integer.toOctalString(from)
				+ "\t\t" + label + i
				+ "\tMMU"
				+ "\t\t" + desc + i
			);
			from += 2;
		}



	}

}
