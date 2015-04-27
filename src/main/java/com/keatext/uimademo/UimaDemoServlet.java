// This code is dedicated to the public domain

package com.keatext.uimademo;

import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.createResourceCreationSpecifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import opennlp.uima.Date;
import opennlp.uima.Location;
import opennlp.uima.Money;
import opennlp.uima.Organization;
import opennlp.uima.Percentage;
import opennlp.uima.Person;
import opennlp.uima.Sentence;
import opennlp.uima.Time;
import opennlp.uima.Token;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.util.XMLInputSource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.uimafit.util.JCasUtil;

import com.keatext.MoneyTransfer;

public class UimaDemoServlet implements Servlet {

	public void destroy() {
	}

	public ServletConfig getServletConfig() {
		return null;
	}

	public String getServletInfo() {
		return "";
	}

	public void init(ServletConfig arg0) throws ServletException {
	}

	private static Object lock = new Object();

	private static AnalysisEngine engine;

	private static CAS cas;

	public void service(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		InputStream is = request.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		StringBuilder doc = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			doc.append(line).append('\n');
			line = br.readLine();
		}

                JSONArray result = new JSONArray();

		synchronized (lock) {
			try {
				if (engine == null) {
					AnalysisEngineDescription descriptor = (AnalysisEngineDescription) createResourceCreationSpecifier(
							new XMLInputSource(
									UimaDemoServlet.class
											.getClassLoader()
											.getResourceAsStream(
													"com/keatext/uimaDemoAggregate.xml"),
									new File(".")), new Object[0]);
					engine = AnalysisEngineFactory.createEngine(descriptor);
					cas = engine.newCAS();
				}
				cas.reset();

				cas.setDocumentText(doc.toString());
				cas.setDocumentLanguage("en");

				// document annotation goes into the (empty) initial view
				DocumentAnnotation documentAnnotation = new DocumentAnnotation(
						cas.getJCas());
				documentAnnotation.setLanguage("en");
				documentAnnotation.addToIndexes();

				engine.process(cas);

				JCas jcas = cas.getJCas();
				for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
					JSONObject obj = new JSONObject();

					JSONArray tokens = new JSONArray();
					for (Token token : JCasUtil.selectCovered(Token.class,
							sentence))
						tokens.put(token.getCoveredText());
					obj.put("tokens", tokens);

					JSONArray people = new JSONArray();
					for (Person person : JCasUtil.selectCovered(Person.class,
							sentence))
						people.put(person.getCoveredText());
					obj.put("people", people);

					JSONArray organizations = new JSONArray();
					for (Organization organization : JCasUtil.selectCovered(
							Organization.class, sentence))
						organizations.put(organization.getCoveredText());
					obj.put("organizations", organizations);

					JSONArray locations = new JSONArray();
					for (Location location : JCasUtil.selectCovered(
							Location.class, sentence))
						locations.put(location.getCoveredText());
					obj.put("locations", locations);

					JSONArray dates = new JSONArray();
					for (Date date : JCasUtil.selectCovered(Date.class,
							sentence))
						dates.put(date.getCoveredText());
					obj.put("dates", dates);

					JSONArray times = new JSONArray();
					for (Time time : JCasUtil.selectCovered(Time.class,
							sentence))
						times.put(time.getCoveredText());
					obj.put("times", times);

					JSONArray moneys = new JSONArray();
					for (Money money : JCasUtil.selectCovered(Money.class,
							sentence))
						moneys.put(money.getCoveredText());
					obj.put("moneys", moneys);

					JSONArray percentages = new JSONArray();
					for (Percentage percentage : JCasUtil.selectCovered(
							Percentage.class, sentence))
						moneys.put(percentage.getCoveredText());
					obj.put("percentages", percentages);

					JSONArray transfers = new JSONArray();
					for (MoneyTransfer transfer : JCasUtil.selectCovered(
							MoneyTransfer.class, sentence))
						transfers.put(transfer.getCoveredText());
					obj.put("transfers", transfers);

					result.put(obj);
				}
			} catch (Exception e) {
				e.printStackTrace(response.getWriter());
			}
		}
                response.getWriter().println(result.toString());
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server(Integer.valueOf(args[0]));
		ServletHolder holder = new ServletHolder(new UimaDemoServlet());
		ServletHandler context = new ServletHandler();
		context.addServletWithMapping(holder, "/");
		server.setHandler(context);
		server.start();
		server.join();
	}
}
