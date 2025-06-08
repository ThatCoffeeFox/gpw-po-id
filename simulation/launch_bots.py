#!/usr/bin/env python3
import subprocess
import time
import sys
import os

# Configuration for each bot (wallet_id, company_id, email, username, password)
# Adjust username/password or emails as needed

COMPANIES = 10
BOTS_PER_COMPANY = 5
FIRST_WALELT_ID = 301

BOTS = []

for company in range(COMPANIES):
    for j in range(BOTS_PER_COMPANY):
        BOTS.append((FIRST_WALELT_ID + company*BOTS_PER_COMPANY + j, company + 1, f'bot{company*BOTS_PER_COMPANY + j + 1}@example.com') )

# Bot script filename
BOT_SCRIPT = "trading_bot.py"

# Base URL of your trading API
BASE_URL = "http://localhost:8080"

# Additional common arguments
RATE_LIMIT = 1
BASE_PRICE = 100.0
MAX_ORDER_SIZE = 5
ORDER_EXPIRATION_SEC = 60
PRICE_VARIATION = 0.01
MAX_RETRIES = 4

def launch_bot(wallet_id, company_id, email):
    cmd = [
        sys.executable, BOT_SCRIPT,
        "--username", email,
        "--password", "funuser1",
        "--wallet_id", str(wallet_id),
        "--company_id", str(company_id),
        "--base_url", BASE_URL,
        "--rate_limit", str(RATE_LIMIT),
        "--base_price", str(BASE_PRICE),
        "--max_order_size", str(MAX_ORDER_SIZE),
        "--order_expiration_sec", str(ORDER_EXPIRATION_SEC),
        "--price_variation", str(PRICE_VARIATION),
        "--max_retries", str(MAX_RETRIES),
    ]

    print(f"Launching bot for wallet {wallet_id}, company {company_id}, email {email}")
    with open(os.devnull, 'w') as devnull:
        return subprocess.Popen(cmd, stdout=devnull, stderr=devnull)


def main():
    processes = []

    for wallet_id, company_id, email in BOTS:
        p = launch_bot(wallet_id, company_id, email)
        processes.append(p)
        time.sleep(0.5)  # stagger start slightly to reduce race conditions

    try:
        while True:
            # Monitor processes
            alive = [p for p in processes if p.poll() is None]
            if not alive:
                print("All bots have exited.")
                break
            time.sleep(5)
    except KeyboardInterrupt:
        print("Terminating bots...")
        for p in processes:
            p.terminate()
        print("Terminated all bots.")

if __name__ == "__main__":
    main()
