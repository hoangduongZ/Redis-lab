package com.redislab.lab03.ticket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * "Noi dau" cua he thong phan tan: synchronized chi khoa duoc trong 1 JVM.
 * Chay 2 instance = 2 bien "sold" rieng trong RAM cua tung tien trinh, nen
 * toi da ban duoc 2 ve du kho chi co dung 1.
 */
@Service
public class NaiveTicketService {

	@Value("${server.port}")
	private String instancePort;

	private boolean sold = false;
	private String soldTo = null;

	public synchronized String buy(String userId) {
		if (sold) {
			return "That bai. Ve da duoc " + soldTo + " mua truoc do (tren instance :" + instancePort + ").";
		}
		sold = true;
		soldTo = userId;
		return "Thanh cong! Ve thuoc ve " + userId + " (tren instance :" + instancePort + ").";
	}

	public synchronized void reset() {
		sold = false;
		soldTo = null;
	}
}
