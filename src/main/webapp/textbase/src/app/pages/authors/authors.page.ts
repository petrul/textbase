import { Component, OnInit, ViewChild } from '@angular/core';
import { RestApiClientService } from 'src/app/rest-api-client.service';
import { Title } from '@angular/platform-browser';
import { IonSearchbar } from '@ionic/angular';
import { AuthorDto } from 'src/app/dto/AuthorDto';

@Component({
  selector: 'app-authors',
  templateUrl: './authors.page.html',
  styleUrls: ['./authors.page.scss'],
})
export class AuthorsPage implements OnInit {

  allAuthors: AuthorDto[] = [];
  authors: AuthorDto[] = [];
  
  @ViewChild(IonSearchbar) searchBar: IonSearchbar;

  constructor(private apiClient: RestApiClientService, private titleService: Title) {
    this.titleService.setTitle('TEXT ❖ BASE Authors');
  }

  genericAvatar(str: string) {
    //const set = "open-peeps";
    //const set = "adventurer";
    const rnd = Math.floor(Math.random() * 10);
    const set = rnd > 1 ? "initials" : "open-peeps";
    const res = `https://avatars.dicebear.com/api/${set}/${str}.svg`;
    
    return res;
  }

  async ngOnInit() {
    await this.retrieveAuthors();
  }

 
  async retrieveAuthors() {
    this.allAuthors = await this.apiClient.getAuthors();
    this.allAuthors.forEach (it => {
      const genericAvatar = 
      it.image_href = it.image_href ? it.image_href : this.genericAvatar(it.strId);
    });
    this.authors = this.allAuthors;
  }

  ionViewDidEnter() {
    this.searchBar.ionChange.subscribe(event => this.doSearch(event));
  }

  doSearch(event) {    
    console.log("Event", event.detail);
    const str = event.detail.value.toLowerCase();
    //this.authors = this.allAuthors.filter(it => it.lastName?.toLowerCase().includes(str) || it.firstName?.toLowerCase().includes(str) );
    this.authors = this.allAuthors.filter(it => it.normalizedName?.toLowerCase().includes(str));
  }

  async doRefresh(event) {
    try {
      await this.retrieveAuthors();
    } finally {
      event.target.complete();
    }
  }
}
