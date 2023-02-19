import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { RestApiClientService } from 'src/app/rest-api-client.service';

@Component({
  selector: 'app-about',
  templateUrl: './about.page.html',
  styleUrls: ['./about.page.scss'],
})
export class AboutPage implements OnInit {

  constructor(private api: RestApiClientService) { }

  serverInfo: any;

  credits = [
    { href: "https://ionicframework.com/",  label: 'Ionic framework'},
    { href: "https://spring.io/",           label: 'Spring framework'},
    { href: "https://tei-c.org/tools/stylesheets/",  label: 'TEI Stylesheets'},
    { href: "https://www.gutenberg.org/", label: 'Project Gutenberg'},
    { href: "https://www.elejandria.com/", label: 'Elejandria'},
    { href: "https://wikisource.org/", label: 'Wikisource'},
    { href: "https://www.liberliber.it/benvenuto/", label: 'Liberliber.it'},
    { href: "https://linux.org/", label: 'The Linux Penguin'},
    { href: "https://www.libreoffice.org/", label: 'Libre Office'},
    { href: "https://git-scm.com/", label: 'Git'},
    { href: "https://www.java.com/en/", label: 'Java'},
    
  ];

  shuffled_credits = [];

  async ngOnInit() {
    this.shuffled_credits = this.shuffleArray(this.credits);
    this.serverInfo = await this.api.getVersion();
  }

  shuffleArray(array) {
    var m = array.length, t, i;
 
    while (m) {
     i = Math.floor(Math.random() * m--);
     t = array[m];
     array[m] = array[i];
     array[i] = t;
    }
 
   return array;
 }

}
