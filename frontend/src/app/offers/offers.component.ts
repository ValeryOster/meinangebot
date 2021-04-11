import {Component, OnInit} from '@angular/core';
import {Offer, StartService} from "../service/start.service";
import {environment} from "../../environments/environment";

@Component({
    selector: 'app-offers',
    templateUrl: './offers.component.html',
    styleUrls: ['./offers.component.css']
})
export class OffersComponent implements OnInit {
    offers: Offer[];
    discounters: Map<string, Map<string, string>> = new Map<string, Map<string, string>>();
    url = environment.apiUrl;
    lidlLogo = this.url + "/logos/lidl.png";
    pennyLogo = this.url  + "/logos/penny.png";
    aldiLogo =  this.url + "/logos/aldi.png";


    constructor(public service: StartService) {
    }

    ngOnInit(): void {
        this.service.getAll().subscribe(value => {
            let strings = Object.keys(value);
            for (let i = 0; i < strings.length; i++) {
                let map = this.mapToDiscount(value[strings[i]]);
                this.discounters.set(strings[i], map);
            }
        })
    }

    mapToDiscount(offers: Offer[]) {
        let discounterMap = new Map();
        offers = offers.sort((a, b) => (a.kategorie < b.kategorie ? -1 : 1))

        let kat = offers.map(value1 => value1.kategorie).filter(
            (value1, index, array) => array.indexOf(value1) == index
        );

        kat.forEach(kategori => {
            let offers1 = offers.filter(value1 => value1.kategorie == kategori);
            discounterMap.set(kategori, offers1);
        })

        return discounterMap;
    }
}
