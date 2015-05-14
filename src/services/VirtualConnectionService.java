package services;

import java.util.Scanner;

public class VirtualConnectionService implements ConnectionService
{
	private boolean isRunning;
	private Scanner sc;
	private boolean inputs[] = new boolean[16];
	private boolean outputs[] = new boolean[16];

	public VirtualConnectionService()
	{
		isRunning = true;
		sc = new Scanner(System.in);

		new Thread(new Runnable() {
			@Override
			public void run()
			{
				System.out.println("Interactive Command Interpreter");
				System.out.println("   inputs=[hex]      - set inputs");
				System.out.println("   outputs=[hex]     - set outputs");
				System.out.println("   inputs            - print inputs");
				System.out.println("   outputs           - print outputs");
				while (isRunning) {
					System.out.print(" > ");
					String args[] = sc.nextLine().split("=");
					if (args[0].trim().equals("inputs")) {
						if (args.length == 1)
						{
							String inputString = "|";
							for (int i = inputs.length - 1; i >= 0; i--)
								inputString += String.format("%02d|", i);

							inputString += "\n|";
							for (int i = inputs.length - 1; i >= 0; i--)
								inputString += (inputs[i] ? " 1|" : " 0|");

							System.out.println(inputString);
							continue;
						}
						setInputs(args[1].trim());
						System.out.println("set input=" + args[1]);


					}
					else if (args[0].trim().equals("outputs")) {
						if (args.length == 1)
						{
							String outputString = "|";
							for (int i = outputs.length - 1; i >= 0; i--)
								outputString += String.format("%02d|", i);

							outputString += "\n|";
							for (int i = outputs.length - 1; i >= 0; i--)
								outputString += (outputs[i] ? " 1|" : " 0|");

							System.out.println(outputString);
							continue;
						}
						setOutputs(args[1].trim());
						System.out.println("set output=" + args[1]);
					}
				}
			}
		}).start();
	}

	void setInputs(String values)
	{
		int binary = Integer.parseInt(values, 16);

		for (int i = 0; i < inputs.length; i++)
			inputs[i] = ((1 << i) & binary) != 0;

	}

	void setOutputs(String values)
	{
		int binary = Integer.parseInt(values, 16);

		for (int i = 0; i < outputs.length; i++)
			outputs[i] = ((1 << i) & binary) != 0;
	}

	@Override
	public boolean[] getInputs() throws Exception
	{
		return inputs;
	}

	@Override
	public boolean[] getOutputs() throws Exception
	{
		return outputs;
	}

	@Override
	public void setOutput(int port, boolean value) throws Exception
	{
		outputs[port] = value;
	}

	@Override
	public void close()
	{
		isRunning = false;
	}
}
