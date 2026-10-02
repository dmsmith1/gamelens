import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
interface Team { id: string; name: string; season: string; }
interface Player { id: string; teamId: string; firstName: string; lastName: string; jerseyNumber: number | null; primaryPosition: string; bats: string; throwsHand: string; }
type PlayerForm = Omit<Player, 'id' | 'teamId'>;
@Component({selector:'app-root', imports:[FormsModule], templateUrl:'./app.html', styleUrl:'./app.css'})
export class App implements OnInit {
 teams=signal<Team[]>([]); selected=signal<Team|null>(null); players=signal<Player[]>([]);
 loading=signal(true); rosterLoading=signal(false); busy=signal(false); error=signal(''); notice=signal(''); fields=signal<Record<string,string>>({});
 teamFormOpen=signal(false); editingTeam=signal<string|null>(null); playerFormOpen=signal(false); editingPlayer=signal<string|null>(null); removal=signal<Player|null>(null);
 teamForm={name:'',season:''}; playerForm:PlayerForm=this.blankPlayer();
 readonly positions=[['P','Pitcher'],['C','Catcher'],['FIRST_BASE','First base'],['SECOND_BASE','Second base'],['THIRD_BASE','Third base'],['SS','Shortstop'],['LF','Left field'],['CF','Center field'],['RF','Right field'],['DH','Designated hitter'],['UTILITY','Utility']];
 constructor(private http:HttpClient) {}
 async ngOnInit() { await this.loadTeams(); }
 blankPlayer():PlayerForm { return {firstName:'',lastName:'',jerseyNumber:null,primaryPosition:'UTILITY',bats:'RIGHT',throwsHand:'RIGHT'}; }
 async loadTeams() {
  this.loading.set(true);this.error.set('');
  try { this.teams.set(await firstValueFrom(this.http.get<Team[]>('/api/teams')));
    let saved:string|null=null;try { saved=localStorage.getItem('gamelens.team'); } catch {}
    const t=this.teams().find(t=>t.id===saved)??this.teams()[0];if(t) await this.select(t);
  } catch(e) { this.fail(e); } finally { this.loading.set(false); }
 }
 resetMessages() { this.error.set('');this.notice.set('');this.fields.set({}); }
 async select(t:Team) {
  this.selected.set(t);this.players.set([]);this.playerFormOpen.set(false);this.teamFormOpen.set(false);this.removal.set(null);this.resetMessages();this.rosterLoading.set(true);
  try {localStorage.setItem('gamelens.team',t.id);}catch {}
  try {const players=await firstValueFrom(this.http.get<Player[]>(`/api/teams/${t.id}/players`));if(this.selected()?.id===t.id)this.players.set(players);}
  catch(e){if(this.selected()?.id===t.id)this.fail(e);}finally{if(this.selected()?.id===t.id)this.rosterLoading.set(false);}
 }
 openTeam(t?:Team) {this.resetMessages();this.playerFormOpen.set(false);this.editingTeam.set(t?.id??null);this.teamForm={name:t?.name??'',season:t?.season??''};this.teamFormOpen.set(true);}
 async saveTeam() {
  this.busy.set(true);this.resetMessages();
  try {const id=this.editingTeam();const t=await firstValueFrom(id?this.http.put<Team>(`/api/teams/${id}`,this.teamForm):this.http.post<Team>('/api/teams',this.teamForm));
    this.teams.update(ts=>[...ts.filter(x=>x.id!==t.id),t].sort((a,b)=>a.name.localeCompare(b.name)||a.season.localeCompare(b.season)));await this.select(t);this.notice.set(id?'Team updated.':'Team created. Add your first player below.');
  }catch(e){this.fail(e);}finally{this.busy.set(false);}
 }
 openPlayer(p?:Player) {this.resetMessages();this.teamFormOpen.set(false);this.editingPlayer.set(p?.id??null);this.playerForm=p?{firstName:p.firstName,lastName:p.lastName,jerseyNumber:p.jerseyNumber,primaryPosition:p.primaryPosition,bats:p.bats,throwsHand:p.throwsHand}:this.blankPlayer();this.playerFormOpen.set(true);}
 async savePlayer() {
  const t=this.selected();if(!t)return;this.busy.set(true);this.resetMessages();
  try {const id=this.editingPlayer();const path=`/api/teams/${t.id}/players`;const data={...this.playerForm,jerseyNumber:this.playerForm.jerseyNumber??null};
    await firstValueFrom(id?this.http.put<Player>(`${path}/${id}`,data):this.http.post<Player>(path,data));
    this.players.set(await firstValueFrom(this.http.get<Player[]>(path)));this.playerFormOpen.set(false);this.notice.set(id?'Player updated.':'Player added.');
  }catch(e){this.fail(e);}finally{this.busy.set(false);}
 }
 async removePlayer() {
  const p=this.removal();const t=this.selected();if(!p||!t)return;this.busy.set(true);this.resetMessages();
  try {await firstValueFrom(this.http.delete(`/api/teams/${t.id}/players/${p.id}`));this.players.update(ps=>ps.filter(x=>x.id!==p.id));this.removal.set(null);this.notice.set('Player removed.');}
  catch(e){this.fail(e);}finally{this.busy.set(false);}
 }
 position(value:string){return this.positions.find(p=>p[0]===value)?.[1]??value;}
 hand(value:string){return value==='LEFT'?'Left':value==='SWITCH'?'Switch':'Right';}
 fail(e:unknown) {const err=e as HttpErrorResponse;this.error.set(err.error?.message??'Could not reach GameLens. Check the connection and try again.');this.fields.set(err.error?.fields??{});}
}
