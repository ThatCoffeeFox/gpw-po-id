#!/usr/bin/env python3

import random
from datetime import datetime

ACCOUNT_NUMBER = 100
WALLETS_PER_ACCOUNT = 3
COMPANY_NUMBER = 10
BOT_NUMBER = 50
IPO_PRICE = 100



pesels = [
    "02281336492", "86052663482", "99031838245", "76111738493", "50040813148",
    "80093094948", "84110357441", "53061427781", "96090429449", "66040239859",
    "89052986391", "80053199953", "92022475382", "68090478365", "90031013825",
    "80022894238", "00252381876", "71052258895", "96052471716", "09270963154",
    "82121381372", "86061624582", "08291924416", "92071197163", "99051024695",
    "78101032132", "54120829917", "94121064531", "93070461495", "51080858115",
    "86031348281", "95092587719", "62060574332", "70072042187", "94111154978",
    "96070545941", "77070497353", "59072031546", "01311241515", "90071773673",
    "73112825195", "83060236367", "98022025459", "84101214287", "09210141147",
    "87021978745", "69010733689", "09262186666", "80061918223", "09232131311",
    "53092421121", "67051237496", "52110717941", "84051025944", "84081043545",
    "60060981893", "01292442521", "79112034315", "80011594886", "03211375448",
    "74090581789", "03210832821", "50021187219", "72102763138", "52071548536",
    "96020559288", "72102978963", "93083145834", "99020676692", "77072324411",
    "06310873934", "92121887648", "53011063649", "90062213487", "51022242826",
    "84042395661", "79112685498", "72033041925", "79012821329", "71122332555",
    "53061671579", "68082667751", "74120268952", "00280944973", "67112115727",
    "63061887458", "51120174146", "57110865243", "04301162829", "94070332473",
    "09262151136", "79112285377", "77121475765", "93092461341", "50061627685",
    "92082489439", "05322882365", "86110693277", "58070987934", "08261843978",
    "89030218355", "62051883647", "09271491452", "05211135316", "75070423668",
    "60112966843", "03312192272", "91011667296", "87042137877", "52081154651",
    "57041282311", "82112989615", "04282959588", "52121976522", "89092269263",
    "02301485979", "02321735458", "66042783691", "87051491155", "56042182576",
    "72021856692", "58052252685", "08260531713", "06322182653", "97091162777",
    "82073162328", "50051459124", "81092577726", "52071541584", "92020998357",
    "63032663535", "60052435511", "87072454454", "65061754668", "91111871122",
    "50022176474", "05250132868", "80101352521", "02210598254", "58111874883",
    "09261653158", "97081411184", "87013085589", "99050339183", "53020634911",
    "89092956532", "65092481344", "57021427938", "92112893645", "95062598444",
    "73100315761", "68090768714", "00262022183", "55043049958", "83020431225",
    "52122121242", "63101987476", "54011748866", "97092614914", "86013041173",
    "88110179818", "97062416889", "50120356972", "81111772998", "04290375796",
    "07321582895", "01211544813", "62112695738", "07311796536", "81122789929",
    "06322318881", "56052335999", "52031242344", "85072888956", "71022573252",
    "67121883864", "06262275996", "98092764975", "00301814698", "97060178354",
    "96091997473", "68021212918", "95071884792", "65030544324", "90101481761",
    "95112854331", "01272445867", "95061685149", "55123072357", "87121763869",
    "82071242642", "62121633518", "61082523841", "53102756588", "88011474919",
    "78113024895", "56111796354", "99071248615", "50061035912", "73082689661"
]


postal_code_town = [
    ("11-500", "8546"), ("11-500", "9168"), ("11-500", "9726"), ("11-500", "11767"),
    ("11-500", "12005"), ("11-500", "12423"), ("11-500", "14661"), ("11-500", "14736"),
    ("11-500", "14737"), ("11-500", "15218"), ("11-500", "22068"), ("11-500", "24379"),
    ("11-500", "24474"), ("11-500", "24569"), ("11-500", "25424"), ("11-500", "28152"),
    ("11-500", "30864"), ("11-500", "31022"), ("11-500", "31985"), ("11-500", "31986"),
    ("11-500", "32678"), ("11-500", "33156"), ("11-500", "33841"), ("11-500", "35463"),
    ("11-500", "35464"), ("11-500", "36723"), ("11-500", "36724"), ("11-500", "36725"),
    ("11-500", "38172"), ("11-500", "38182"), ("11-500", "38183"), ("11-500", "39858"),
    ("11-510", "823"), ("11-510", "956"), ("11-510", "4623"), ("11-510", "4837"),
    ("11-510", "6605"), ("11-510", "7454"), ("11-510", "7630"), ("11-510", "7631"),
    ("11-510", "7739"), ("11-510", "9227"), ("11-510", "9335"), ("11-510", "9879"),
    ("11-510", "15530"), ("11-510", "17926"), ("11-510", "18673"), ("11-510", "19207"),
    ("11-510", "22811"), ("11-510", "23132"), ("11-510", "23839"), ("11-510", "23860"),
    ("11-510", "23861"), ("11-510", "27208"), ("11-510", "27366"), ("11-510", "27947"),
    ("11-510", "28151"), ("11-510", "29485"), ("11-510", "29596"), ("11-510", "32571"),
    ("11-510", "33064"), ("11-510", "33157"), ("11-510", "34101"), ("11-510", "36141"),
    ("11-510", "38341"), ("11-510", "38342"), ("11-510", "39705"), ("11-513", "1250"),
    ("11-513", "2008"), ("11-513", "5171"), ("11-513", "5335"), ("11-513", "10547"),
    ("11-513", "10548"), ("11-513", "12918"), ("11-513", "13905"), ("11-513", "13908"),
    ("11-513", "13909"), ("11-513", "16847"), ("11-513", "16914"), ("11-513", "18923"),
    ("11-513", "19460"), ("11-513", "19834"), ("11-513", "19835"), ("11-513", "23882"),
    ("11-513", "28233"), ("11-513", "28244"), ("11-513", "28642"), ("11-513", "31886"),
    ("11-513", "31887"), ("11-513", "36434"), ("11-513", "38615"), ("11-520", "310"),
    ("11-520", "3425"), ("11-520", "8021"), ("11-520", "9919"), ("11-520", "11364"),
    ("11-520", "13239"), ("11-520", "13240"), ("11-520", "15100"), ("11-520", "15101"),
    ("11-520", "15587"), ("11-520", "17676"), ("11-520", "19927"), ("11-520", "20024"),
    ("11-520", "20358"), ("11-520", "20614"), ("11-520", "23125"), ("11-520", "26100"),
    ("11-520", "28557"), ("11-520", "28683"), ("11-520", "28701"), ("11-520", "28703"),
    ("11-520", "29511"), ("11-520", "30035"), ("11-520", "30063"), ("11-520", "30247"),
    ("11-520", "31256"), ("11-520", "31987"), ("11-520", "33555"), ("11-520", "34659"),
    ("11-520", "35990"), ("11-520", "39861"), ("11-600", "1151"), ("11-600", "2793"),
    ("11-600", "5076"), ("11-600", "5555"), ("11-600", "5883"), ("11-600", "9658"),
    ("11-600", "10610"), ("11-600", "10785"), ("11-600", "11325"), ("11-600", "11725"),
    ("11-600", "11813"), ("11-600", "11917"), ("11-600", "11985"), ("11-600", "12747"),
    ("11-600", "12991"), ("11-600", "13622"), ("11-600", "17503"), ("11-600", "17883"),
    ("11-600", "18443"), ("11-600", "19173"), ("11-600", "22711"), ("11-600", "23952"),
    ("11-600", "24029"), ("11-600", "24195"), ("11-600", "24628"), ("11-600", "25041"),
    ("11-600", "26272"), ("11-600", "26676"), ("11-600", "27213"), ("11-600", "28196"),
    ("11-600", "28197"), ("11-600", "28396"), ("11-600", "28466"), ("11-600", "31911"),
    ("11-600", "31920"), ("11-600", "32189"), ("11-600", "32490"), ("11-600", "32775"),
    ("11-600", "33414"), ("11-600", "33415"), ("11-600", "34172"), ("11-600", "34704"),
    ("11-600", "36046"), ("11-600", "36066"), ("11-600", "36103"), ("11-600", "36755"),
    ("11-600", "38499"), ("11-606", "1793"), ("11-606", "2825"), ("11-606", "2963"),
    ("11-606", "3070"), ("11-606", "5531"), ("11-606", "6306"), ("11-606", "6402"),
    ("11-606", "8949"), ("11-606", "9216"), ("11-606", "11650"), ("11-606", "14707"),
    ("11-606", "20137"), ("11-606", "22935")
]

password = "$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi"

founds_needed = [0] * ( WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER + 1 + BOT_NUMBER)
shares_needed=[[0 for _ in range(COMPANY_NUMBER + 1)] for _ in range(WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER + 1 + BOT_NUMBER)]

orders = []
transactions = []
transefers = []
subscriptions = []

print("\\encoding UTF8")
print("COPY accounts (role) FROM stdin WITH(FORMAT csv);")
print("admin")
for i in range(1, ACCOUNT_NUMBER + BOT_NUMBER):
    print("user")
print("\\.")
print()

print("COPY accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) FROM stdin WITH(FORMAT csv);")
for i in range(ACCOUNT_NUMBER):
    id = i + 1
    updatet_at = "2025-05-01 00:00:00"
    email = f"user{id}@example.com"
    first_name = f"User {id}"
    middle_name = "Middle name"
    last_name = "Kowalski"
    postal_code, town_id = postal_code_town[random.randint(0,len(postal_code_town) - 1)]
    street = "Spacerowa"
    street_number = id
    apartment_number = id
    phone_number = "+48" + "".join(str(random.randint(0, 9)) for _ in range(9))
    pesel = pesels[i]
    print(f'{id},"{updatet_at}","{email}","{password}","{first_name}","{middle_name}","{last_name}",{town_id},"{postal_code}","{street}","{street_number}","{apartment_number}","{phone_number}","{pesel}"')
for i in range(ACCOUNT_NUMBER, ACCOUNT_NUMBER + BOT_NUMBER):
    id = i + 1
    updatet_at = "2025-05-01 00:00:00"
    email = f"bot{id - ACCOUNT_NUMBER}@example.com"
    first_name = f"Bot {id-ACCOUNT_NUMBER}"
    middle_name = "Middle name"
    last_name = "Kowalski"
    postal_code, town_id = postal_code_town[random.randint(0,len(postal_code_town) - 1)]
    street = "Spacerowa"
    street_number = id
    apartment_number = id
    phone_number = "+48" + "".join(str(random.randint(0, 9)) for _ in range(9))
    pesel = pesels[i]
    print(f'{id},"{updatet_at}","{email}","{password}","{first_name}","{middle_name}","{last_name}",{town_id},"{postal_code}","{street}","{street_number}","{apartment_number}","{phone_number}","{pesel}"')
print("\\.")
print()

print("COPY wallets(account_id, name, active) FROM stdin WITH(FORMAT csv);") 
for i in range(ACCOUNT_NUMBER):
    for j in range(WALLETS_PER_ACCOUNT):
        wallet_id = 1 + WALLETS_PER_ACCOUNT*i + j
        account_id = 1 + i
        name = f"User {account_id} Wallet {j + 1}"
        active = "true"
        print(f'{account_id},"{name}",{active}')
for i in range(BOT_NUMBER):
    wallet_id = WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER + 1 + i
    account_id = ACCOUNT_NUMBER + i + 1
    name = f"Bot {i + 1} Wallet"
    active = "true"
    print(f'{account_id},"{name}",{active}')
    for j in range(COMPANY_NUMBER):
        founds_needed[wallet_id] += 100000 + 1000 * IPO_PRICE
        shares_needed[wallet_id][j + 1] = 1000

print("\\.")
print()

for i in range(COMPANY_NUMBER):
    print("INSERT INTO companies DEFAULT VALUES;")

def int_to_code(n):
    if not (0 <= n < 26**3):
        raise ValueError("n must be between 0 and 17575 (inclusive)")
    chars = []
    for i in range(3):
        n, r = divmod(n, 26)
        chars.append(chr(ord('A') + r))
    return ''.join(reversed(chars))

print("COPY companies_info(company_id, updated_at, name, code, town_id, postal_code, street, street_number, apartment_number) FROM stdin WITH(FORMAT csv);")
for i in range(COMPANY_NUMBER):
    company_id = i + 1
    updatet_at = "2025-05-01 00:00:01"
    name = f'Firma {company_id}'
    code = int_to_code(company_id)
    postal_code, town_id = postal_code_town[i]
    street = "Frimowa"
    street_number = company_id
    apartment_number = company_id
    print(f'{company_id},"{updatet_at}","{name}","{code}","{town_id}","{postal_code}","{street}","{street_number}","{apartment_number}"')

print("\\.")
print()


print("COPY ipo(company_id, payment_wallet_id, shares_amount, ipo_price, subscription_start, subscription_end, processed) FROM stdin WITH(FORMAT csv);")
for i in range(COMPANY_NUMBER):
    ipo_id = i + 1
    company_id = i + 1
    payment_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)
    shares_amount = 1000000
    ipo_price = IPO_PRICE
    subscription_start = "2025-05-01 00:00:02"
    subscription_end = "2025-05-02 23:59:59"
    processed = "true"
    print(f'{company_id},{payment_wallet_id},{shares_amount},{ipo_price},"{subscription_start}","{subscription_end}",{processed}')

print("\\.")
print()

print("COPY companies_status(company_id, date, tradable) FROM stdin WITH(FORMAT csv);")
for i in range(COMPANY_NUMBER):
    company_id = i + 1
    date = "2025-05-03 00:00:00"
    tradable = "true"
    print(f'{company_id},"{date}",{tradable}')

print("\\.")
print()

now = datetime.now()
now.day



for company_id in range(1,COMPANY_NUMBER + 1):
    last_price = IPO_PRICE
    for day in range (3, 30):
        for hour in [0, 7, 12, 19]:
            new_price = round(max(1, last_price * random.uniform(0.95,1.05)),2)
            last_price = new_price
            shares_amount = random.randint(1,5)
            
            buy_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)
            sell_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)

            buy_order_id = len(orders) + 1
            sell_order_id = len(orders) + 2

            order_start_date = f"2025-05-{str(day).zfill(2)} {str(hour).zfill(2)}:00:04"
            order_expiration_date = f"2025-05-{str(day).zfill(2)} {str(hour).zfill(2)}:01:04"
            transaction_date = f"2025-05-{str(day).zfill(2)} {str(hour).zfill(2)}:00:05"

            orders.append(f'"buy",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{buy_wallet_id},{company_id}')
            orders.append(f'"sell",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{sell_wallet_id},{company_id}')

            founds_needed[buy_wallet_id] += new_price * shares_amount
            shares_needed[sell_wallet_id][company_id] += shares_amount
            founds_needed[sell_wallet_id] += shares_amount * IPO_PRICE

            transactions.append(f'{sell_order_id},{buy_order_id},"{transaction_date}",{shares_amount},{new_price}')

    for day in range(1, now.day):
        for hour in range(24):
            new_price = round(max(1, last_price * random.uniform(0.95,1.05)),2)
            last_price = new_price
            shares_amount = random.randint(1,5)
            
            buy_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)
            sell_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)

            buy_order_id = len(orders) + 1
            sell_order_id = len(orders) + 2

            order_start_date = f"2025-06-{str(day).zfill(2)} {str(hour).zfill(2)}:00:04"
            order_expiration_date = f"2025-06-{str(day).zfill(2)} {str(hour).zfill(2)}:01:04"
            transaction_date = f"2025-06-{str(day).zfill(2)} {str(hour).zfill(2)}:00:05"

            orders.append(f'"buy",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{buy_wallet_id},{company_id}')
            orders.append(f'"sell",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{sell_wallet_id},{company_id}')
            
            founds_needed[buy_wallet_id] += new_price * shares_amount
            shares_needed[sell_wallet_id][company_id] += shares_amount
            founds_needed[sell_wallet_id] += shares_amount * IPO_PRICE

            transactions.append(f'{sell_order_id},{buy_order_id},"{transaction_date}",{shares_amount},{new_price}')
    for hour in range(now.hour):
        for minute in  [0, 10, 20, 30, 40, 50]:
            new_price = round(max(1, last_price * random.uniform(0.95,1.05)),2)
            last_price = new_price
            shares_amount = random.randint(1,5)
            
            buy_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)
            sell_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)

            buy_order_id = len(orders) + 1
            sell_order_id = len(orders) + 2

            order_start_date = f"2025-06-{str(now.day).zfill(2)} {str(hour).zfill(2)}:{str(minute).zfill(2)}:04"
            order_expiration_date = f"2025-06-{str(now.day).zfill(2)} {str(hour).zfill(2)}:{str(minute).zfill(2)}:06"
            transaction_date = f"2025-06-{str(now.day).zfill(2)} {str(hour).zfill(2)}:{str(minute).zfill(2)}:05"

            orders.append(f'"buy",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{buy_wallet_id},{company_id}')
            orders.append(f'"sell",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{sell_wallet_id},{company_id}')
            
            founds_needed[buy_wallet_id] += new_price * shares_amount
            shares_needed[sell_wallet_id][company_id] += shares_amount
            founds_needed[sell_wallet_id] += shares_amount * IPO_PRICE

            transactions.append(f'{sell_order_id},{buy_order_id},"{transaction_date}",{shares_amount},{new_price}')
    for minute in range(now.minute):
        for seccond in [0, 10, 20, 30, 40, 50]:
            new_price = round(max(1, last_price * random.uniform(0.95,1.05)),2)
            last_price = new_price
            shares_amount = random.randint(1,5)
            
            buy_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)
            sell_wallet_id = random.randint(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER)

            buy_order_id = len(orders) + 1
            sell_order_id = len(orders) + 2

            order_start_date = f"2025-06-{str(now.day).zfill(2)} {str(now.hour).zfill(2)}:{str(minute).zfill(2)}:{str(seccond + 4).zfill(2)}"
            order_expiration_date = f"2025-06-{str(now.day).zfill(2)} {str(now.hour).zfill(2)}:{str(minute).zfill(2)}:{str(seccond + 6).zfill(2)}"
            transaction_date = f"2025-06-{str(now.day).zfill(2)} {str(now.hour).zfill(2)}:{str(minute).zfill(2)}:{str(seccond + 5).zfill(2)}"

            orders.append(f'"buy",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{buy_wallet_id},{company_id}')
            orders.append(f'"sell",{shares_amount},"{order_start_date}","{order_expiration_date}",{new_price},{sell_wallet_id},{company_id}')
            
            founds_needed[buy_wallet_id] += new_price * shares_amount
            shares_needed[sell_wallet_id][company_id] += shares_amount
            founds_needed[sell_wallet_id] += shares_amount * IPO_PRICE

            transactions.append(f'{sell_order_id},{buy_order_id},"{transaction_date}",{shares_amount},{new_price}')


for wallet_id in range(1, WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER + 1 + BOT_NUMBER):
    if(founds_needed[wallet_id] == 0):
        continue
    transfer_id = len(transefers) + 1
    type = "deposit"
    amount = founds_needed[wallet_id]
    date = "2025-05-01 00:00:02"
    account_nuber = '00000000000000000000000000'
    transefers.append(f'{wallet_id},"{type}","{date}",{amount},"{account_nuber}"')

for wallet_id in range(1,  WALLETS_PER_ACCOUNT * ACCOUNT_NUMBER + 1 + BOT_NUMBER):
    for ipo_id in range(1, COMPANY_NUMBER + 1):
        if(shares_needed[wallet_id][ipo_id] == 0):
            continue
        subscription_id = len(subscriptions) + 1
        date = "2025-05-01 00:01:02"
        shares_amount = shares_needed[wallet_id][ipo_id]
        shares_asigned = shares_needed[wallet_id][ipo_id]
        subscriptions.append(f'{ipo_id},{wallet_id},"{date}",{shares_amount},{shares_asigned}')

print("COPY external_transfers(wallet_id, type, date, amount, account_number) FROM stdin WITH(FORMAT csv);")
for transfer in transefers:
    print(transfer)

print("\\.")
print()

print("COPY subscriptions(ipo_id, wallet_id, date, shares_amount, shares_assigned) FROM stdin WITH(FORMAT csv);")
for subscription in subscriptions:
    print(subscription)
print("\\.")
print()

print("COPY orders(order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) FROM stdin WITH(FORMAT csv);")
for order in orders:
    print(order)
print("\\.")
print()

print("COPY transactions(sell_order_id, buy_order_id, date, shares_amount, share_price) FROM stdin WITH(FORMAT csv);")
for transaction in transactions:\
    print(transaction)
print("\\.")
print()